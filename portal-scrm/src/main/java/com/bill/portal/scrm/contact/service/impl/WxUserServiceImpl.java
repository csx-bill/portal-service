package com.bill.portal.scrm.contact.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bill.portal.scrm.contact.dto.WxUserQuery;
import com.bill.portal.scrm.contact.entity.WxDepartment;
import com.bill.portal.scrm.contact.entity.WxUser;
import com.bill.portal.scrm.contact.mapper.WxDepartmentMapper;
import com.bill.portal.scrm.contact.mapper.WxUserMapper;
import com.bill.portal.scrm.contact.service.IWxUserService;
import org.apache.commons.lang3.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class WxUserServiceImpl extends ServiceImpl<WxUserMapper, WxUser> implements IWxUserService {

    private final WxDepartmentMapper wxDepartmentMapper;

    /**
     * 条件仅对非空字段生效；部门过滤用 JSON_CONTAINS 命中
     * depart_ids（JSON 数组）中的任一元素，与企微"部门下全体成员"语义一致
     */
    @Override
    public IPage<WxUser> page(Page<WxUser> page, WxUserQuery query) {
        return baseMapper.selectPage(page, buildWrapper(query));
    }

    @Override
    public List<WxUser> list(WxUserQuery query) {
        return baseMapper.selectList(buildWrapper(query));
    }

    private LambdaQueryWrapper<WxUser> buildWrapper(WxUserQuery query) {
        return Wrappers.<WxUser>lambdaQuery()
                .like(StringUtils.isNotBlank(query.getName()), WxUser::getName, query.getName())
                .eq(query.getStatus() != null, WxUser::getStatus, query.getStatus())
                .eq(StringUtils.isNotBlank(query.getCorpId()), WxUser::getCorpId, query.getCorpId())
                .apply(query.getDeptId() != null, "JSON_CONTAINS(depart_ids, JSON_ARRAY({0}))", query.getDeptId())
                .orderByAsc(WxUser::getId);
    }

    /**
     * 部门（含子部门）下的成员企微 userid 列表：成员按主部门归属匹配部门子树
     */
    @Override
    public List<String> listUserIdsByDeptTree(String corpId, Long deptId) {
        Set<Long> subtree = deptSubtreeIds(corpId, deptId);
        if (subtree.isEmpty()) {
            return List.of();
        }
        return baseMapper.selectList(Wrappers.<WxUser>lambdaQuery()
                        .eq(StringUtils.isNotBlank(corpId), WxUser::getCorpId, corpId)
                        .in(WxUser::getMainDepartment, subtree))
                .stream()
                .map(WxUser::getUserId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
    }

    /**
     * 收集部门及其全部子部门的企微部门ID（按 parentId 建父子映射后广度遍历）
     */
    private Set<Long> deptSubtreeIds(String corpId, Long deptId) {
        List<WxDepartment> depts = wxDepartmentMapper.selectList(
                Wrappers.<WxDepartment>lambdaQuery()
                        .eq(StringUtils.isNotBlank(corpId), WxDepartment::getCorpId, corpId));
        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (WxDepartment dept : depts) {
            if (dept.getParentId() != null) {
                childrenMap.computeIfAbsent(dept.getParentId(), k -> new ArrayList<>())
                        .add(dept.getDeptId());
            }
        }
        Set<Long> result = new HashSet<>();
        List<Long> queue = new ArrayList<>();
        queue.add(deptId);
        while (!queue.isEmpty()) {
            Long current = queue.remove(0);
            if (result.add(current)) {
                queue.addAll(childrenMap.getOrDefault(current, List.of()));
            }
        }
        return result;
    }
}
