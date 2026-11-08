package com.bill.portal.common.core.client;

import cn.dev33.satoken.oauth2.data.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.template.SaOAuth2Util;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 客户端拦截器 —— 解析当前请求的客户端标识并设置到上下文。
 * 请求头 Client-Id 是客户端可控输入，不可直接信任：
 * 有登录态时以 OAuth2 令牌签发的 clientId 为准（防伪造头跨客户端读写数据），
 * 无令牌场景（登录前接口、开放接口）回退取请求头。
 */
@Slf4j
public class ClientInterceptor implements HandlerInterceptor {

    public static final String HEADER_CLIENT_ID = "Client-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        String clientId = null;
        try {
            AccessTokenModel at = SaOAuth2Util.currentAccessToken();
            if (at != null && at.getClientId() != null && !at.getClientId().isEmpty()) {
                clientId = at.getClientId();
            }
        } catch (Exception e) {
            log.debug("令牌客户端解析跳过: {}", e.getMessage());
        }
        if (clientId == null) {
            clientId = request.getHeader(HEADER_CLIENT_ID);
        }
        if (clientId != null && !clientId.isEmpty()) {
            ClientContextHolder.setClientId(clientId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        ClientContextHolder.clear();
    }
}
