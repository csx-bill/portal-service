package com.bill.portal.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bill.portal.common.core.client.ClientContextHolder;
import com.bill.portal.system.constant.MenuTypeConstants;
import com.bill.portal.system.constant.RoleConstants;
import com.bill.portal.system.entity.SysMenu;
import com.bill.portal.system.entity.SysRoleMenu;
import com.bill.portal.system.mapper.SysMenuMapper;
import com.bill.portal.system.mapper.SysRoleMenuMapper;
import com.bill.portal.system.service.ISysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    private final SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public List<SysMenu> getMenuTree() {
        String clientId = ClientContextHolder.getClientId();
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getClientId,clientId)
                .orderByAsc(SysMenu::getSortOrder));

        // 根节点 pid == null 或 pid == 0
        List<SysMenu> roots = allMenus.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .collect(Collectors.toList());

        for (SysMenu root : roots) {
            buildChildren(root, allMenus);
        }

        return roots;
    }

    @Override
    public List<SysMenu> getMyMenuTree() {
        String clientId = ClientContextHolder.getClientId();
        // 当前登录用户角色（StpInterfaceImpl：sys_user_role 的角色 ID）
        List<Long> roleIds = StpUtil.getRoleList().stream()
                .map(Long::valueOf)
                .toList();

        // 可路由菜单：启用中的目录/菜单（按钮不进路由）
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getClientId, clientId)
                .eq(SysMenu::getStatus, 1)
                .ne(SysMenu::getMenuType, MenuTypeConstants.BUTTON)
                .orderByAsc(SysMenu::getSortOrder));

        List<SysMenu> visibleMenus;
        if (roleIds.contains(RoleConstants.SUPER_ADMIN_ROLE_ID)) {
            visibleMenus = allMenus;
        } else {
            if (roleIds.isEmpty()) {
                return List.of();
            }
            Set<Long> grantedMenuIds = sysRoleMenuMapper.selectList(
                            Wrappers.<SysRoleMenu>lambdaQuery().in(SysRoleMenu::getRoleId, roleIds))
                    .stream()
                    .map(SysRoleMenu::getMenuId)
                    .collect(Collectors.toSet());
            // 保留授权菜单及其全部祖先目录，保证树连通
            Map<Long, SysMenu> menuById = allMenus.stream()
                    .collect(Collectors.toMap(SysMenu::getId, m -> m));
            Set<Long> keepIds = new HashSet<>();
            for (SysMenu menu : allMenus) {
                if (!grantedMenuIds.contains(menu.getId())) {
                    continue;
                }
                SysMenu current = menu;
                while (current != null && keepIds.add(current.getId())) {
                    current = menuById.get(current.getParentId());
                }
            }
            visibleMenus = allMenus.stream()
                    .filter(m -> keepIds.contains(m.getId()))
                    .toList();
        }

        List<SysMenu> roots = visibleMenus.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .collect(Collectors.toList());
        for (SysMenu root : roots) {
            buildChildren(root, visibleMenus);
        }
        return roots;
    }

    private void buildChildren(SysMenu parent, List<SysMenu> allMenus) {
        List<SysMenu> children = allMenus.stream()
                .filter(m -> parent.getId().equals(m.getParentId()))
                .sorted(Comparator.comparing(SysMenu::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        parent.setChildren(children);

        for (SysMenu child : children) {
            buildChildren(child, allMenus);
        }
    }

    public boolean isNameExists(String name, Long excludeId) {
        String clientId = ClientContextHolder.getClientId();
        return lambdaQuery()
                .eq(SysMenu::getName, name)
                .eq(SysMenu::getClientId, clientId)
                .ne(excludeId != null, SysMenu::getId, excludeId)
                .exists();
    }

    public boolean isPathExists(String path, Long excludeId) {
        String clientId = ClientContextHolder.getClientId();
        return lambdaQuery()
                .eq(SysMenu::getPath, path)
                .eq(SysMenu::getClientId, clientId)
                .ne(excludeId != null, SysMenu::getId, excludeId)
                .exists();
    }
}
