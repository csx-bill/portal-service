package com.bill.portal.common.core.security;

import java.util.List;

/**
 * 用户权限/角色数据加载接口（RBAC）。
 * 由具体业务模块（如 portal-system）实现并注册为 Bean；
 * 无实现模块时 StpInterfaceImpl 自动降级为空集合
 */
public interface IPermissionService {

    /**
     * 获取指定账号的权限标识集合
     * @param loginId 登录账号 ID
     * @return 权限标识集合，可含 "clientId:authCode" 形式的客户端限定标识
     */
    List<String> getPermissionList(Object loginId);

    /**
     * 获取指定账号的角色标识集合
     * @param loginId 登录账号 ID
     * @return 角色标识集合
     */
    List<String> getRoleList(Object loginId);
}
