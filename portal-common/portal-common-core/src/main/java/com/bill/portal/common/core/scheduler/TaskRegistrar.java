package com.bill.portal.common.core.scheduler;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务注册器接口 —— 连接引擎与数据库的桥梁。
 *
 * 每个业务模块实现此接口并注册为 Spring Bean，引擎在启动时自动发现并接管调度。
 *
 * @param <T> 业务实体类型（需实现 TaskInstance）
 */
public interface TaskRegistrar<T extends TaskInstance> {

    /** 注册器唯一标识，用于日志区分（如 "mass-task"、"order-timeout"） */
    String name();

    /** 实体类型 */
    Class<T> taskType();

    /** 对应的业务执行器 */
    TaskExecutor<T> executor();

    // ────── 加载（引擎从数据库读取任务） ──────

    /** 启动恢复：查询所有启用状态的周期任务（status=1 AND execute_type>=2） */
    List<T> loadActiveTasks();

    /** 兜底轮询：查询已到期但未执行的任务 */
    List<T> loadDueTasks();

    /** 按主键加载单个任务，返回 null 表示不存在 */
    T loadById(Long taskId);

    // ────── 持久化（引擎执行后回写数据库） ──────

    /**
     * CAS 抢锁：原子地将 status 从 1(启用) 改为 2(执行中)。
     * 返回 true 表示抢锁成功，false 表示已被其他节点抢走或状态已变。
     *
     * 典型 SQL：UPDATE {table} SET status = 2 WHERE id = ? AND status = 1
     */
    boolean casLock(Long taskId);

    /** 执行成功：更新下次执行时间 + 恢复启用状态 */
    void onSuccess(Long taskId, LocalDateTime nextTime);

    /** 执行失败：恢复为启用状态，等待重试 */
    void onFailure(Long taskId);

    /** 任务完成（无下次执行）：标记为已完成 */
    void onComplete(Long taskId);
}
