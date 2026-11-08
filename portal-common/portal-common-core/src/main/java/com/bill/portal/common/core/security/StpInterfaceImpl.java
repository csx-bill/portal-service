package com.bill.portal.common.core.security;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限数据源（通用实现）：
 * 委托 IPermissionService 获取权限/角色集合，具体 RBAC 查询由业务模块提供；
 * 容器中无 IPermissionService 实现时返回空集合（fail-closed）
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    private final ObjectProvider<IPermissionService> permissionServiceProvider;

    public StpInterfaceImpl(ObjectProvider<IPermissionService> permissionServiceProvider) {
        this.permissionServiceProvider = permissionServiceProvider;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        IPermissionService service = permissionServiceProvider.getIfAvailable();
        return service == null ? List.of() : service.getPermissionList(loginId);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        IPermissionService service = permissionServiceProvider.getIfAvailable();
        return service == null ? List.of() : service.getRoleList(loginId);
    }
}
