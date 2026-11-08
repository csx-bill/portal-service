package com.bill.portal.system.datascope;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bill.portal.common.core.datascope.DataScopeScopeResolver;
import com.bill.portal.common.core.datascope.DataScopeView;
import com.bill.portal.system.entity.SysRole;
import com.bill.portal.system.entity.SysRoleDept;
import com.bill.portal.system.entity.SysUserDept;
import com.bill.portal.system.entity.SysUserRole;
import com.bill.portal.system.mapper.SysRoleDeptMapper;
import com.bill.portal.system.mapper.SysRoleMapper;
import com.bill.portal.system.mapper.SysUserDeptMapper;
import com.bill.portal.system.mapper.SysUserRoleMapper;
import com.bill.portal.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据范围解析实现（sys_* 表的计算全部收口在本模块）：
 * 用户→角色→data_scope→可见系统部门(sys_role_dept/本级/本级及子级)→部门下的账号。
 * 多角色取并集（含"全部"即全量）；无角色/可见集为空 → DENY。
 * 相关表均已登记 tenant.tables，租户条件由租户拦截器自动注入。
 */
@Service
@RequiredArgsConstructor
public class DataScopeScopeResolverImpl implements DataScopeScopeResolver {

    private static final int SCOPE_ALL = 1;
    private static final int SCOPE_CUSTOM = 2;
    private static final int SCOPE_DEPT_AND_CHILD = 3;
    private static final int SCOPE_DEPT_ONLY = 4;

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleDeptMapper sysRoleDeptMapper;
    private final SysUserDeptMapper sysUserDeptMapper;
    private final ISysDeptService sysDeptService;

    @Override
    public DataScopeView resolveScope(Long userId) {
        // 用户的角色ID集合
        List<Long> roleIds = sysUserRoleMapper.selectList(
                        Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).distinct().toList();
        if (roleIds.isEmpty()) {
            return DataScopeView.deny();
        }

        // 角色的 data_scope 集合（多角色并集语义：含"全部"即全量）
        Set<Integer> scopes = sysRoleMapper.selectBatchIds(roleIds)
                .stream().map(SysRole::getDataScope).collect(Collectors.toSet());
        if (scopes.contains(SCOPE_ALL)) {
            return DataScopeView.all();
        }

        // 可见系统部门集合：自定义(角色登记) ∪ 本级(账号部门) ∪ 本级及子级(含子树)
        Set<Long> visibleDeptIds = new HashSet<>();
        if (scopes.contains(SCOPE_CUSTOM)) {
            sysRoleDeptMapper.selectList(Wrappers.<SysRoleDept>lambdaQuery()
                            .in(SysRoleDept::getRoleId, roleIds))
                    .forEach(rd -> visibleDeptIds.add(rd.getDeptId()));
        }
        List<Long> ownDeptIds = sysUserDeptMapper.selectList(
                        Wrappers.<SysUserDept>lambdaQuery().eq(SysUserDept::getUserId, userId))
                .stream().map(SysUserDept::getDeptId).distinct().toList();
        if (scopes.contains(SCOPE_DEPT_ONLY)) {
            visibleDeptIds.addAll(ownDeptIds);
        }
        if (scopes.contains(SCOPE_DEPT_AND_CHILD)) {
            for (Long deptId : ownDeptIds) {
                visibleDeptIds.addAll(sysDeptService.listSelfAndChildIds(deptId));
            }
        }
        if (visibleDeptIds.isEmpty()) {
            return DataScopeView.deny();
        }

        // 可见部门 → 账号集合
        List<Long> userIds = sysUserDeptMapper.selectList(
                        Wrappers.<SysUserDept>lambdaQuery().in(SysUserDept::getDeptId, visibleDeptIds))
                .stream().map(SysUserDept::getUserId).distinct().toList();
        return DataScopeView.ofUsers(userIds);
    }
}
