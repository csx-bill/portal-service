package com.bill.portal.common.core.security;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.handler.SaAnnotationHandlerInterface;
import cn.dev33.satoken.annotation.handler.SaCheckPermissionHandler;
import com.bill.portal.common.core.client.ClientContextHolder;

import java.lang.reflect.AnnotatedElement;

/**
 * 支持 #clientId 占位符的 @SaCheckPermission 处理器。
 *
 * 权限码与数据库 sys_menu.auth_code 配置值原样精确匹配，支持两种约定：
 * 1. 公共功能（跨客户端共享）：auth_code 存全量格式（如 "web-scrm:user:add"），
 *    注解写 "#clientId:user:add"，运行时将 #clientId 解析为请求头 Client-Id 后匹配；
 * 2. 非公共功能（仅单个客户端的业务功能）：auth_code 存裸码（如 "mass:update"），
 *    注解直接写 "mass:update"，原样透传匹配。
 *
 * 请求未携带 Client-Id 且使用了 #clientId 占位符时解析为空串，校验必然失败（fail-closed）
 */
public class ClientScopeCheckPermissionHandler implements SaAnnotationHandlerInterface<SaCheckPermission> {

    public static final String PLACEHOLDER = "#clientId:";

    @Override
    public Class<SaCheckPermission> getHandlerAnnotationClass() {
        return SaCheckPermission.class;
    }

    @Override
    public void checkMethod(SaCheckPermission at, AnnotatedElement element) {
        String clientId = ClientContextHolder.getClientId();
        String[] resolved = new String[at.value().length];
        for (int i = 0; i < at.value().length; i++) {
            String permission = at.value()[i];
            resolved[i] = permission.startsWith(PLACEHOLDER)
                    ? (clientId == null ? "" : clientId) + permission.substring(PLACEHOLDER.length())
                    : permission;
        }
        SaCheckPermissionHandler._checkMethod(at.type(), resolved, at.mode(), at.orRole());
    }
}
