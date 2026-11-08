package com.bill.portal.common.core.datascope;

import cn.dev33.satoken.oauth2.data.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.template.SaOAuth2Util;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 数据权限拦截器 —— 从 OAuth2 令牌解析当前登录用户ID（sys_user.id）
 * 并写入 {@link DataScopeContextHolder}，供各模块 Handler 构造过滤条件；
 * Job/回调等无令牌场景 userId 为空，Handler 自行决定放行或拒绝。
 */
@Slf4j
public class DataScopeInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            AccessTokenModel at = SaOAuth2Util.currentAccessToken();
            if (at != null && at.getLoginId() != null) {
                DataScopeContextHolder.setUserId(((Number) at.getLoginId()).longValue());
            }
        } catch (Exception e) {
            log.debug("数据权限上下文解析跳过: {}", e.getMessage());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        DataScopeContextHolder.clear();
    }
}
