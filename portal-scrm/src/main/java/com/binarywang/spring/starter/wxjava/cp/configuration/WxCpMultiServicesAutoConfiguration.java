package com.binarywang.spring.starter.wxjava.cp.configuration;

import com.bill.portal.scrm.core.annotation.ConditionalOnSelfMode;
import com.binarywang.spring.starter.wxjava.cp.configuration.services.WxCpInJedisConfiguration;
import com.binarywang.spring.starter.wxjava.cp.configuration.services.WxCpInMemoryConfiguration;
import com.binarywang.spring.starter.wxjava.cp.configuration.services.WxCpInRedisTemplateConfiguration;
import com.binarywang.spring.starter.wxjava.cp.configuration.services.WxCpInRedissonConfiguration;
import com.binarywang.spring.starter.wxjava.cp.properties.WxCpMultiProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 自建应用模式总控配置
 * 只有当 xqt.wx.mode=SELF 时才激活
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({WxCpMultiProperties.class})
@Import({WxCpInJedisConfiguration.class, WxCpInMemoryConfiguration.class, WxCpInRedissonConfiguration.class, WxCpInRedisTemplateConfiguration.class})
@ConditionalOnSelfMode
public class WxCpMultiServicesAutoConfiguration {
    @PostConstruct
    public void init() {
        log.info("当前加载配置模式: [SELF]");
    }
}
