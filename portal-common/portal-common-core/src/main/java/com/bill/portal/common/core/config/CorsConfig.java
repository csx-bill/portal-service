package com.bill.portal.common.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    /**
     * 配置跨域资源共享(CORS)过滤器
     * 允许前端应用从不同域访问后端API
     */
    @Bean
    public CorsFilter corsFilter() {
        // 创建CORS配置对象
        CorsConfiguration config = new CorsConfiguration();

        // 允许所有来源的跨域请求
        // 注意：在生产环境中，应该指定具体的域名，而不是使用"*"
        config.addAllowedOriginPattern("*");

        // 允许所有请求头
        // 包括Content-Type, Authorization等常用请求头
        config.addAllowedHeader("*");

        // 允许所有HTTP方法
        // 包括GET, POST, PUT, DELETE, OPTIONS等
        config.addAllowedMethod("*");

        // 允许发送Cookie和认证信息
        // 当设置为true时，addAllowedOrigin不能设置为"*"
        config.setAllowCredentials(true);

        // 创建基于URL的CORS配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 将CORS配置应用到所有路径
        source.registerCorsConfiguration("/**", config);

        // 返回CORS过滤器
        return new CorsFilter(source);
    }

}
