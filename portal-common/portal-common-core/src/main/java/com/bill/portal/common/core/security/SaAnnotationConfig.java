package com.bill.portal.common.core.security;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Sa-Token 注解校验定制：用支持 #clientId 占位符的处理器替换默认的 @SaCheckPermission 处理器。
 * annotationHandlerMap 为全局单例，启动时替换一次即可生效（@SaCheckOr 内嵌注解同样走该 map）
 */
@Configuration
public class SaAnnotationConfig {

    @PostConstruct
    public void customizeCheckPermissionHandler() {
        SaAnnotationStrategy.instance.annotationHandlerMap.put(
                SaCheckPermission.class, new ClientScopeCheckPermissionHandler());
    }
}
