package com.bill.portal.common.core.scheduler;

/**
 * 调度类型枚举。
 *
 * <pre>
 * 业务表 execute_type 列直接存枚举名（VARCHAR）：
 *   IMMEDIATE  立即执行
 *   ONCE       仅一次
 *   DAILY      每天
 *   WEEKLY     每周
 *   MONTHLY    每月
 *   CRON       自定义 Cron 表达式
 * </pre>
 */
public enum ScheduleType {

    /** 立即执行：执行完就结束，无下次 */
    IMMEDIATE,

    /** 仅一次：在 scheduled_time 执行一次后结束 */
    ONCE,

    /** 每天：按 baseTime 的时间点每天执行 */
    DAILY,

    /** 每周：按 baseTime 的星期几和时间点每周执行 */
    WEEKLY,

    /** 每月：按 baseTime 的日期和时间点每月执行 */
    MONTHLY,

    /** 自定义 Cron：按 cron_expression 计算 */
    CRON;

    /** 是否为周期类型（有下次执行） */
    public boolean isRecurring() {
        return this == DAILY || this == WEEKLY || this == MONTHLY || this == CRON;
    }
}
