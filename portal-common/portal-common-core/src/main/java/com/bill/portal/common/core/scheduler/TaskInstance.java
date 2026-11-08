package com.bill.portal.common.core.scheduler;

import java.time.LocalDateTime;

/**
 * 任务实例接口 —— 业务实体接入调度引擎的唯一契约。
 *
 * <pre>
 * 接入示例：
 *   {@code @Data}
 *   {@code @TableName("my_task")}
 *   public class MyTask implements TaskInstance {
 *       private Long id;
 *       private Integer status;
 *
 *       {@code @Override public Long getTaskId() { return id; }}
 *       {@code @Override public Integer getStatus() { return status; }}
 *       {@code @Override public ScheduleType getScheduleType() { return ScheduleType.DAILY; }}
 *       {@code @Override public String getCronExpression() { return null; }}
 *       {@code @Override public LocalDateTime getBaseTime() { return scheduledTime; }}
 *   }
 * </pre>
 */
public interface TaskInstance {

    /** 任务唯一标识（对应数据库主键） */
    Long getTaskId();

    /**
     * 任务状态。
     * 约定值：1-启用 2-执行中 3-已完成 4-禁用。
     * 引擎只调度 status=1 的任务。
     */
    Integer getStatus();

    /**
     * 调度类型，决定如何计算下次执行时间。
     * 引擎根据此值调用 CronHelper 对应方法。
     */
    ScheduleType getScheduleType();

    /**
     * Cron 表达式（仅 ScheduleType.CRON 时使用）。
     * 其他类型返回 null。
     */
    String getCronExpression();

    /**
     * 基准时间（对应数据库 DATETIME 类型）。
     * DAILY/WEEKLY/MONTHLY 类型基于此时间偏移计算下次执行时间。
     */
    LocalDateTime getBaseTime();
}
