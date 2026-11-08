package com.bill.portal.common.core.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全配置（security.*）。
 * ignore-urls 为免认证地址：命中这些路径的请求跳过【所有】拦截器
 * （租户/客户端/数据权限/Sa-Token 校验），适用于登录、OAuth2、
 * 第三方回调等无令牌无请求头的入口；上下文由业务自行构造。
 */
@Data
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    /** 免认证地址（Ant 风格），统一在 application.yml 的 security.ignore-urls 中配置 */
    private List<String> ignoreUrls = new ArrayList<>();

}
