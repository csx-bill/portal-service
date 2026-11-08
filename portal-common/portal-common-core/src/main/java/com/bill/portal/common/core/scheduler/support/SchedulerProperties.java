package com.bill.portal.common.core.scheduler.support;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;

/**
 * 调度器配置属性。
 *
 * <pre>
 * application.yml 示例：
 *   scheduler:
 *     zone: Asia/Shanghai
 *     thread-pool-size: 8
 *     catch-up-interval-ms: 30000
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {

    /** 时区，默认系统时区 */
    private ZoneId zone = ZoneId.systemDefault();

    /** 精准定时器线程池大小，默认 8 */
    private int threadPoolSize = 8;

    /** 兜底轮询间隔(ms)，默认 30000（30秒） */
    private int catchUpIntervalMs = 30_000;
}
