package com.bill.portal.system.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bill.portal.common.core.security.IPermissionService;
import com.bill.portal.system.constant.RoleConstants;
import com.bill.portal.system.entity.SysMenu;
import com.bill.portal.system.entity.SysRoleMenu;
import com.bill.portal.system.entity.SysUserRole;
import com.bill.portal.system.mapper.SysMenuMapper;
import com.bill.portal.system.mapper.SysRoleMenuMapper;
import com.bill.portal.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * RBAC 权限数据实现：
 * 从 用户-角色 -> 角色-菜单 -> 菜单权限标识(authCode) 链路装配当前账号的权限集合。
 * 权限码按数据库配置原样返回（约定全量格式 "<clientId>:<action>"，如 web-scrm:user:delete），
 * 与 @SaCheckPermission("#clientId:<action>") 运行时解析结果直接匹配
 */
@Component
@RequiredArgsConstructor
public class PermissionServiceImpl implements IPermissionService {

    private final SysUserRoleMapper sysUserRoleMapper;

    private final SysRoleMenuMapper sysRoleMenuMapper;

    private final SysMenuMapper sysMenuMapper;

    @Override
    public List<String> getPermissionList(Object loginId) {
        List<Long> roleIds = listRoleIds(loginId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        // 内置超级管理员：返回系统内全部权限标识
        if (roleIds.contains(RoleConstants.SUPER_ADMIN_ROLE_ID)) {
            return listAllPermissions();
        }
        List<Long> menuIds = sysRoleMenuMapper.selectList(
                        Wrappers.<SysRoleMenu>lambdaQuery().in(SysRoleMenu::getRoleId, roleIds))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .toList();
        if (menuIds.isEmpty()) {
            return List.of();
        }
        return sysMenuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                        .in(SysMenu::getId, menuIds)
                        .eq(SysMenu::getStatus, 1)
                        .isNotNull(SysMenu::getAuthCode))
                .stream()
                .map(SysMenu::getAuthCode)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    /**
     * 超级管理员：装配系统内全部权限标识（所有客户端启用中的 authCode 原样下发）
     */
    private List<String> listAllPermissions() {
        return sysMenuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                        .eq(SysMenu::getStatus, 1)
                        .isNotNull(SysMenu::getAuthCode))
                .stream()
                .map(SysMenu::getAuthCode)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    @Override
    public List<String> getRoleList(Object loginId) {
        return listRoleIds(loginId).stream().map(String::valueOf).toList();
    }

    private List<Long> listRoleIds(Object loginId) {
        return sysUserRoleMapper.selectList(
                        Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, Long.valueOf(loginId.toString())))
                .stream()
                .map(SysUserRole::getRoleId)
                .distinct()
                .toList();
    }
}
