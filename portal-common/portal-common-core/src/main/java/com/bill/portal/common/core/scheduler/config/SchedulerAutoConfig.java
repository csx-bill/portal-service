package com.bill.portal.common.core.scheduler.config;

import com.bill.portal.common.core.scheduler.SchedulerEngine;
import com.bill.portal.common.core.scheduler.TaskRegistrar;
import com.bill.portal.common.core.scheduler.support.SchedulerProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

/**
 * 调度器自动配置。
 */
@Configuration
@EnableConfigurationProperties(SchedulerProperties.class)
@EnableScheduling
@EnableAsync
public class SchedulerAutoConfig {

    private final SchedulerEngine engine;
    private final List<TaskRegistrar<?>> customRegs;

    public SchedulerAutoConfig(SchedulerEngine engine,
                               List<TaskRegistrar<?>> customRegs) {
        this.engine = engine;
        this.customRegs = customRegs;
    }

    /** 调度引擎 */
    @Bean
    @ConditionalOnMissingBean
    public static SchedulerEngine schedulerEngine(ObjectProvider<SchedulerProperties> propsProvider) {
        SchedulerProperties props = propsProvider.getIfAvailable(SchedulerProperties::new);
        return new SchedulerEngine(props.getZone(), props.getThreadPoolSize(), props.getCatchUpIntervalMs());
    }


    /** 启动时注册所有模块 + 恢复调度 */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        for (TaskRegistrar<?> r : customRegs) engine.register(r);
        engine.recovery();
    }
}
