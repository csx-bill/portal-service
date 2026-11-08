package com.bill.portal.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bill.portal.common.core.exception.CheckedException;
import com.bill.portal.system.entity.SysDept;
import com.bill.portal.system.entity.SysUserDept;
import com.bill.portal.system.mapper.SysDeptMapper;
import com.bill.portal.system.mapper.SysUserDeptMapper;
import com.bill.portal.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    private final SysUserDeptMapper sysUserDeptMapper;

    @Override
    public List<SysDept> tree() {
        List<SysDept> all = list(Wrappers.<SysDept>lambdaQuery()
                .orderByAsc(SysDept::getSortOrder)
                .orderByAsc(SysDept::getId));
        return buildTree(all);
    }

    @Override
    public List<Long> listSelfAndChildIds(Long deptId) {
        List<SysDept> all = list();
        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (SysDept dept : all) {
            if (dept.getParentId() != null) {
                childrenMap.computeIfAbsent(dept.getParentId(), k -> new ArrayList<>()).add(dept.getId());
            }
        }
        List<Long> result = new ArrayList<>();
        List<Long> queue = new ArrayList<>();
        queue.add(deptId);
        while (!queue.isEmpty()) {
            Long current = queue.remove(0);
            result.add(current);
            queue.addAll(childrenMap.getOrDefault(current, List.of()));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(SysDept entity) {
        // 上级部门不能是自身或自己的子孙节点，否则形成环
        if (entity.getParentId() != null && entity.getId() != null
                && listSelfAndChildIds(entity.getId()).contains(entity.getParentId())) {
            throw new CheckedException("dept.parentInvalid");
        }
        return super.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        // 存在子部门时拒绝删除，避免悬挂引用
        boolean hasChildren = lambdaQuery().eq(SysDept::getParentId, id).exists();
        if (hasChildren) {
            throw new CheckedException("dept.hasChildren");
        }
        // 同步清理用户-部门关联
        sysUserDeptMapper.delete(new LambdaQueryWrapper<SysUserDept>()
                .eq(SysUserDept::getDeptId, id));
        return super.removeById(id);
    }

    /**
     * 组装部门树；parentId 指向的父节点不存在（或为空）时视为根部门
     */
    private List<SysDept> buildTree(List<SysDept> all) {
        Map<Long, List<SysDept>> byParent = new HashMap<>();
        for (SysDept dept : all) {
            if (dept.getParentId() != null) {
                byParent.computeIfAbsent(dept.getParentId(), k -> new ArrayList<>()).add(dept);
            }
        }
        List<SysDept> roots = new ArrayList<>();
        for (SysDept dept : all) {
            dept.setChildren(byParent.getOrDefault(dept.getId(), new ArrayList<>()));
            boolean parentMissing = dept.getParentId() == null
                    || all.stream().noneMatch(d -> d.getId().equals(dept.getParentId()));
            if (parentMissing) {
                roots.add(dept);
            }
        }
        return roots;
    }
}
