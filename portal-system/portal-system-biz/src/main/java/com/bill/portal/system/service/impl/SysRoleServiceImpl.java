package com.bill.portal.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bill.portal.common.core.exception.CheckedException;
import com.bill.portal.system.constant.RoleConstants;
import com.bill.portal.system.entity.SysDept;
import com.bill.portal.system.entity.SysRole;
import com.bill.portal.system.entity.SysRoleDept;
import com.bill.portal.system.entity.SysRoleMenu;
import com.bill.portal.system.mapper.SysDeptMapper;
import com.bill.portal.system.mapper.SysRoleDeptMapper;
import com.bill.portal.system.mapper.SysRoleMapper;
import com.bill.portal.system.mapper.SysRoleMenuMapper;
import com.bill.portal.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysRoleMenuMapper sysRoleMenuMapper;

    private final SysRoleDeptMapper sysRoleDeptMapper;

    private final SysDeptMapper sysDeptMapper;

    @Override
    public boolean isNameExists(String name, Long excludeId) {
        return lambdaQuery()
                .eq(SysRole::getName, name)
                .ne(excludeId != null, SysRole::getId, excludeId)
                .exists();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysRole entity) {
        // 保存角色基本信息
        super.save(entity);
        // 保存角色-菜单关联
        saveRoleMenus(entity.getId(), entity.getPermissions());
        // 保存自定义数据权限的部门关联
        saveRoleDepts(entity.getId(), entity.getDeptIds());
        return true;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysRole entity) {
        // 更新角色基本信息
        super.updateById(entity);
        // 编辑表单与菜单授权分离：仅在显式传入 permissions 时才重建角色-菜单关联，
        // 否则不带 permissions 的普通编辑（改名称/状态/备注/数据权限）会清空已有授权
        if (entity.getPermissions() != null) {
            // 删除旧关联，保存新关联
            sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                    .eq(SysRoleMenu::getRoleId, entity.getId()));
            saveRoleMenus(entity.getId(), entity.getPermissions());
        }
        // 仅在显式传入 deptIds 时重建自定义数据权限的部门关联
        if (entity.getDeptIds() != null) {
            sysRoleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>()
                    .eq(SysRoleDept::getRoleId, entity.getId()));
            saveRoleDepts(entity.getId(), entity.getDeptIds());
        }
        return super.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        checkNotBuiltInRole(id);
        // 清理角色-部门关联，避免悬挂引用
        sysRoleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>()
                .eq(SysRoleDept::getRoleId, id));
        return super.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeBatchByIds(Collection<?> list) {
        // 先整体校验：任一为内置角色则整批拒绝，不做部分删除
        list.forEach(id -> checkNotBuiltInRole((Serializable) id));
        list.forEach(id -> removeById((Serializable) id));
        return true;
    }

    /**
     * 内置超级管理员角色是系统根权限的载体，不允许删除
     */
    private void checkNotBuiltInRole(Serializable id) {
        if (RoleConstants.SUPER_ADMIN_ROLE_ID == Long.parseLong(id.toString())) {
            throw new CheckedException("内置超级管理员角色不允许删除");
        }
    }

    @Override
    public SysRole getById(Serializable id) {
        SysRole role = super.getById(id);
        if (role != null) {
            // 查询关联的菜单ID
            List<Long> menuIds = sysRoleMenuMapper.selectList(
                            new LambdaQueryWrapper<SysRoleMenu>()
                                    .eq(SysRoleMenu::getRoleId, id))
                    .stream()
                    .map(rm -> rm.getMenuId())
                    .collect(Collectors.toList());
            role.setPermissions(menuIds);
            fillDepts(List.of(role));
        }
        return role;
    }

    @Override
    public <E extends IPage<SysRole>> E pageWithRelations(E page, Wrapper<SysRole> wrapper) {
        E result = super.page(page, wrapper);
        // 为每个角色加载权限
        for (SysRole role : result.getRecords()) {
            List<Long> menuIds = sysRoleMenuMapper.selectList(
                    new LambdaQueryWrapper<SysRoleMenu>()
                            .eq(SysRoleMenu::getRoleId, role.getId()))
                    .stream()
                    .map(rm -> rm.getMenuId())
                    .collect(Collectors.toList());
            role.setPermissions(menuIds);
        }
        fillDepts(result.getRecords());
        return result;
    }

    /**
     * 保存角色-菜单关联
     */
    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) return;
        for (Long menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            sysRoleMenuMapper.insert(rm);
        }
    }

    /**
     * 批量回填自定义数据权限的部门 ID 与名称
     */
    private void fillDepts(List<SysRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return;
        }
        List<Long> roleIds = roles.stream().map(SysRole::getId).collect(Collectors.toList());
        List<SysRoleDept> relations = sysRoleDeptMapper.selectList(
                new LambdaQueryWrapper<SysRoleDept>().in(SysRoleDept::getRoleId, roleIds));
        if (relations.isEmpty()) {
            return;
        }
        List<Long> deptIds = relations.stream().map(SysRoleDept::getDeptId).distinct()
                .collect(Collectors.toList());
        Map<Long, String> nameMap = sysDeptMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getName));
        Map<Long, List<Long>> deptIdsByRole = relations.stream()
                .collect(Collectors.groupingBy(SysRoleDept::getRoleId,
                        Collectors.mapping(SysRoleDept::getDeptId, Collectors.toList())));
        for (SysRole role : roles) {
            List<Long> ids = deptIdsByRole.getOrDefault(role.getId(), List.of());
            role.setDeptIds(ids);
        }
    }

    /**
     * 保存自定义数据权限的角色-部门关联
     */
    private void saveRoleDepts(Long roleId, List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) return;
        for (Long deptId : deptIds) {
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(roleId);
            rd.setDeptId(deptId);
            sysRoleDeptMapper.insert(rd);
        }
    }
}
