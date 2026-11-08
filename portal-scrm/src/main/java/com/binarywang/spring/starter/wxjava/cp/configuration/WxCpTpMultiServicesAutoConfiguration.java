package com.binarywang.spring.starter.wxjava.cp.configuration;

import com.binarywang.spring.starter.wxjava.cp.configuration.services.WxCpTpInJedisTpConfiguration;
import com.binarywang.spring.starter.wxjava.cp.configuration.services.WxCpTpInMemoryTpConfiguration;
import com.binarywang.spring.starter.wxjava.cp.configuration.services.WxCpTpInRedisTemplateTpConfiguration;
import com.binarywang.spring.starter.wxjava.cp.configuration.services.WxCpTpInRedissonTpConfiguration;
import com.binarywang.spring.starter.wxjava.cp.properties.WxCpTpMultiProperties;
import com.bill.portal.scrm.core.annotation.ConditionalOnSuiteMode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 服务商模式总控配置
 * 只有当 xqt.wx.mode=SUITE 时才激活
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({WxCpTpMultiProperties.class})
@Import({WxCpTpInJedisTpConfiguration.class, WxCpTpInMemoryTpConfiguration.class, WxCpTpInRedissonTpConfiguration.class, WxCpTpInRedisTemplateTpConfiguration.class})
@ConditionalOnSuiteMode
public class WxCpTpMultiServicesAutoConfiguration {
    @PostConstruct
    public void init() {
        log.info("当前加载配置模式: [SUITE]");
    }
}
