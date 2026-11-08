package com.bill.portal.common.core.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 租户拦截器 —— 从请求头 X-Tenant-Id 中提取租户 ID 并设置到上下文
 */
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        String tenantIdStr = request.getHeader(HEADER_TENANT_ID);
        if (tenantIdStr != null && !tenantIdStr.isEmpty()) {
            try {
                Long tenantId = Long.valueOf(tenantIdStr);
                TenantContextHolder.setTenantId(tenantId);
            } catch (NumberFormatException e) {
                log.warn("Invalid tenant ID in header: {}", tenantIdStr);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantContextHolder.clear();
    }
}
