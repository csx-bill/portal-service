package com.bill.portal.common.core.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 调度引擎 —— 核心。
 *
 * <pre>
 * 核心流程：
 *   schedule(id) → cancelTimer + loadById + computeNext + timer.schedule
 *
 *   定时器触发 (±50ms)：
 *     executeSafely(id, registrar)
 *       ├── reload DB（状态 != 1? 跳过）
 *       ├── casLock → 再次 reload
 *       ├── executor.execute()
 *       └── onSuccess / onFailure → scheduleNext 递归
 *
 * 实时更新（两层防护）：
 *   ① 主动通知：编辑/启停时调用 refresh(id) → cancel + reschedule，0ms 生效
 *   ② 执行前校验：定时器触发时 reload DB，状态不对就跳过，防竞态
 *
 * 集群安全：由 TaskRegistrar.casLock() 实现（WHERE status=1 原子更新）
 * 启动恢复：recovery() 加载所有启用周期任务并重新调度
 * 兜底轮询：catchUp() 每 N 秒捡漏（重启间隙遗漏的到期任务）
 * </pre>
 */
@Slf4j
public class SchedulerEngine {

    /** 时区 */
    private final ZoneId zone;

    /** 精准定时线程池 */
    private final ScheduledExecutorService timer;

    /** 兜底轮询间隔(ms) */
    private final int catchUpIntervalMs;

    /** taskId → 定时器句柄（用于取消） */
    private final Map<Long, ScheduledFuture<?>> runningTimers = new ConcurrentHashMap<>();

    /** registrar.name() → 注册器 */
    private final Map<String, TaskRegistrar<?>> registrars = new ConcurrentHashMap<>();

    public SchedulerEngine(ZoneId zone, int threadPoolSize, int catchUpIntervalMs) {
        this.zone = zone;
        this.timer = Executors.newScheduledThreadPool(threadPoolSize);
        this.catchUpIntervalMs = catchUpIntervalMs;
    }

    // ================================================================
    //  注册 & 恢复
    // ================================================================

    /** 注册一个业务模块 */
    public void register(TaskRegistrar<?> registrar) {
        registrars.put(registrar.name(), registrar);
        log.info("注册: {}", registrar.name());
    }

    /** 启动恢复：加载所有启用周期任务，重新精准定时 */
    public void recovery() {
        for (TaskRegistrar<?> r : registrars.values()) {
            try {
                var tasks = r.loadActiveTasks();
                log.info("[{}] 恢复: {} 条", r.name(), tasks.size());
                for (var t : tasks) scheduleNext(t, r);
            } catch (Exception e) {
                log.error("[{}] 恢复失败", r.name(), e);
            }
        }
    }

    // ================================================================
    //  公开 API（业务层调用入口）
    // ================================================================

    /**
     * 调度 / 重新调度一个任务。
     * 取消旧定时器 → 从 DB 加载最新状态 → 重新计算并精准定时。
     * 适用场景：新任务创建、编辑、启用。
     */
    public void schedule(Long taskId) {
        cancelTimer(taskId);
        for (TaskRegistrar<?> r : registrars.values()) {
            TaskInstance task = r.loadById(taskId);
            if (task != null) {
                if (task.getStatus() != null && task.getStatus() == 1) {
                    scheduleNext(task, r);
                }
                return;
            }
        }
    }

    /** 取消定时器（不修改数据库状态） */
    public void cancel(Long taskId) {
        cancelTimer(taskId);
    }

    /** 同 schedule()，语义别名 */
    public void refresh(Long taskId) {
        schedule(taskId);
    }

    // ================================================================
    //  核心：计算下一次 + 精准定时
    // ================================================================

    /**
     * 计算下次执行时间，存入 DB，精准定时。
     * cron.next() → 计算 delay → timer.schedule()
     */
    @SuppressWarnings("unchecked")
    private void scheduleNext(TaskInstance task, TaskRegistrar<?> registrar) {
        if (!task.getScheduleType().isRecurring()) return;

        var nextOpt = CronHelper.computeNext(
            task.getScheduleType(), task.getCronExpression(), task.getBaseTime(), zone);

        if (nextOpt.isEmpty()) {
            registrar.onComplete(task.getTaskId());
            runningTimers.remove(task.getTaskId());
            return;
        }

        Instant next = nextOpt.get();
        long delayMs = next.toEpochMilli() - System.currentTimeMillis();

        // 已过期 → 立即执行
        if (delayMs <= 0) {
            executeSafely(task.getTaskId(), (TaskRegistrar) registrar);
            return;
        }

        // 安全上限（防 NTP 回拨）
        long maxDelay = CronHelper.maxDelayMs(task.getScheduleType());
        if (delayMs > maxDelay) {
            log.warn("[{}] 延迟异常 {}ms，降级60s: id={}",
                     registrar.name(), delayMs, task.getTaskId());
            delayMs = 60_000;
        }

        Long taskId = task.getTaskId();
        TaskRegistrar rawReg = (TaskRegistrar) registrar;
        ScheduledFuture<?> future = timer.schedule(
            () -> executeSafely(taskId, rawReg),
            delayMs, TimeUnit.MILLISECONDS);
        runningTimers.put(taskId, future);
    }

    // ================================================================
    //  安全执行：定时器触发 → reload DB 校验 → 执行
    // ================================================================

    /**
     * 执行前从 DB 重新加载，验证任务仍然有效，然后执行业务逻辑。
     * 这是实时更新的第二道防线：即使 cancel 和触发发生竞态，这里也会拦住。
     */
    @Async
    @SuppressWarnings("unchecked")
    public <T extends TaskInstance> void executeSafely(Long taskId, TaskRegistrar<T> registrar) {
        runningTimers.remove(taskId);

        // ① reload DB：任务可能已被删除/禁用
        T task = registrar.loadById(taskId);
        if (task == null || task.getStatus() == null || task.getStatus() != 1) {
            return;
        }

        // ② CAS 抢锁（集群防重）
        if (!registrar.casLock(taskId)) return;

        // ③ 执行业务
        log.info("[{}] ▶ 执行: id={}", registrar.name(), taskId);
        try {
            registrar.executor().execute(task);
            log.info("[{}] ✓ 成功: id={}", registrar.name(), taskId);
        } catch (Exception e) {
            log.error("[{}] ✗ 失败: id={}", registrar.name(), taskId, e);
            registrar.onFailure(taskId);
            T refreshed = registrar.loadById(taskId);
            if (refreshed != null && refreshed.getStatus() == 1) {
                scheduleNext(refreshed, (TaskRegistrar) registrar);
            }
            return;
        }

        // ④ 计算下一次，递归调度
        var nextOpt = CronHelper.computeNext(
            task.getScheduleType(), task.getCronExpression(), task.getBaseTime(), zone);

        if (nextOpt.isEmpty()) {
            registrar.onComplete(taskId);
        } else {
            registrar.onSuccess(taskId, LocalDateTime.ofInstant(nextOpt.get(), zone));
            T refreshed = registrar.loadById(taskId);
            if (refreshed != null) scheduleNext(refreshed, (TaskRegistrar) registrar);
        }
    }

    // ================================================================
    //  兜底轮询：捡漏（重启间隙遗漏的到期任务）
    // ================================================================

    @Scheduled(fixedDelayString = "${scheduler.catch-up-interval-ms:30000}")
    @SuppressWarnings("unchecked")
    public void catchUp() {
        for (TaskRegistrar<?> r : registrars.values()) {
            try {
                var dueTasks = r.loadDueTasks();
                for (var t : dueTasks) {
                    Long taskId = t.getTaskId();
                    if (runningTimers.containsKey(taskId)) continue;
                    long delay = t.getBaseTime().atZone(zone).toInstant().toEpochMilli()
                                 - System.currentTimeMillis();
                    if (delay <= 0) {
                        executeSafely(taskId, (TaskRegistrar) r);
                    } else if (delay <= catchUpIntervalMs * 2L) {
                        scheduleNext(t, r);
                    }
                }
            } catch (Exception e) {
                log.error("[{}] 兜底异常", r.name(), e);
            }
        }
    }

    /** 移除内存中的定时句柄 */
    private void cancelTimer(Long taskId) {
        ScheduledFuture<?> future = runningTimers.remove(taskId);
        if (future != null) future.cancel(false);
    }
}
