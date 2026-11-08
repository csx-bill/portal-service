package com.bill.portal.common.core.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Bean Validation 国际化配置。
 *
 * 校验注解的 message 写成 "{key}" 形式时，从 classpath 的
 * messages*.properties 解析，语言由请求头 Accept-Language 决定
 * （前端请求拦截器已统一携带该头）。本 Bean 覆盖自动装配的
 * defaultValidator，使校验器使用 Spring MessageSource 插值。
 */
@Configuration
public class ValidationConfig {

    @Bean
    public LocalValidatorFactoryBean defaultValidator(MessageSource messageSource) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }
}
