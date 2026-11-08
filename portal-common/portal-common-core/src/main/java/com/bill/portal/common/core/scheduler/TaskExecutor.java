package com.bill.portal.common.core.scheduler;

/**
 * 任务执行器接口 —— 业务方实现此接口，编写具体的任务逻辑。
 *
 * <pre>
 * 示例：
 *   {@code @Component}
 *   public class CleanTempHandler implements TaskExecutor<SchedulerTask> {
 *       {@code @Override}
 *       public void execute(SchedulerTask task) throws Exception {
 *           String params = task.getTaskData();
 *           fileService.cleanTemp(params);
 *       }
 *   }
 * </pre>
 *
 * @param <T> 业务实体类型（需实现 TaskInstance）
 */
@FunctionalInterface
public interface TaskExecutor<T extends TaskInstance> {

    /**
     * 执行业务逻辑。
     * 引擎已处理：抢锁、状态更新、递归调度。
     * 业务方只需写具体逻辑，异常时引擎自动恢复任务为启用状态。
     */
    void execute(T task) throws Exception;

    /**
     * 此执行器支持的调度类型列表，默认支持全部。
     * 可按需覆写以限制类型。
     */
    default ScheduleType[] supportedTypes() {
        return ScheduleType.values();
    }
}
