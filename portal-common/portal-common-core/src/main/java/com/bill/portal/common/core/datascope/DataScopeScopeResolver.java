package com.bill.portal.common.core.datascope;

/**
 * 数据范围解析 SPI：由掌握"用户→角色→部门"数据的模块实现
 * （如 system 模块：角色 data_scope → 可见系统部门 → 部门下的系统账号）。
 * 数据权限路由器在每请求首次需要时调用一次，结果缓存在
 * {@link DataScopeContextHolder} 供各模块 Handler 读取。
 * 未提供实现 Bean 时，业务 Handler 自行决定放行/拒绝。
 */
public interface DataScopeScopeResolver {

    /**
     * @param userId 登录用户ID(sys_user.id)
     * @return 可见范围视图（租户条件由租户拦截器注入，按当前租户计算）
     */
    DataScopeView resolveScope(Long userId);
}
