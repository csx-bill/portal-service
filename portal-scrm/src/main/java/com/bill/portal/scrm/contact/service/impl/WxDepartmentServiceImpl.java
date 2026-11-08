package com.bill.portal.scrm.contact.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bill.portal.scrm.contact.entity.WxDepartment;
import com.bill.portal.scrm.contact.mapper.WxDepartmentMapper;
import com.bill.portal.scrm.contact.service.IWxDepartmentService;
import com.bill.portal.scrm.contact.vo.WxDepartmentTreeVO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class WxDepartmentServiceImpl extends ServiceImpl<WxDepartmentMapper, WxDepartment> implements IWxDepartmentService {

    @Override
    public List<WxDepartmentTreeVO> deptTree() {
        List<WxDepartment> allDepts = list();

        if (allDepts.isEmpty()) {
            return Collections.emptyList();
        }

        // 按 corpId 分组，每组独立构建树，最后平级汇总
        return allDepts.stream()
                .collect(Collectors.groupingBy(WxDepartment::getCorpId))
                .values().stream()
                .flatMap(deptList -> buildTree(deptList).stream())
                .collect(Collectors.toList());
    }

    /**
     * 单个 corpId 下的部门列表构建树，返回 VO
     */
    private List<WxDepartmentTreeVO> buildTree(List<WxDepartment> deptList) {
        // 转为 Hutool TreeNode
        List<TreeNode<Long>> nodeList = deptList.stream()
                .map(dept -> {
                    TreeNode<Long> node = new TreeNode<>(
                            dept.getDeptId(),
                            dept.getParentId(),
                            dept.getName(),
                            dept.getOrder() != null ? dept.getOrder().intValue() : 0
                    );
                    // 将整个 WxDepartment 存入 extra，方便后续转换
                    node.setExtra(Map.of("dept", dept));
                    return node;
                })
                .collect(Collectors.toList());

        // 使用 Hutool TreeUtil 构建树
        List<Tree<Long>> treeList = TreeUtil.build(nodeList, 0L);

        // Tree 转为 WxDepartmentTreeVO
        return treeList.stream()
                .map(this::treeToVO)
                .collect(Collectors.toList());
    }

    /**
     * Hutool Tree 节点递归转为 WxDepartmentTreeVO
     */
    private WxDepartmentTreeVO treeToVO(Tree<Long> tree) {
        WxDepartment dept = (WxDepartment) tree.get("dept");
        WxDepartmentTreeVO vo = new WxDepartmentTreeVO();
        vo.setId(dept.getId());
        vo.setCorpId(dept.getCorpId());
        vo.setDeptId(dept.getDeptId());
        vo.setName(dept.getName());
        vo.setNameEn(dept.getNameEn());
        vo.setParentId(dept.getParentId());
        vo.setOrder(dept.getOrder());
        vo.setDepartmentLeader(dept.getDepartmentLeader());
        vo.setSyncVersion(dept.getSyncVersion());
        vo.setCreateBy(dept.getCreateBy());
        vo.setUpdateBy(dept.getUpdateBy());
        vo.setCreateTime(dept.getCreateTime());
        vo.setUpdateTime(dept.getUpdateTime());
        vo.setDelFlag(dept.getDelFlag());
        vo.setTenantId(dept.getTenantId());

        // 递归转换子节点
        List<Tree<Long>> children = tree.getChildren();
        if (children != null && !children.isEmpty()) {
            vo.setChildren(children.stream()
                    .map(this::treeToVO)
                    .collect(Collectors.toList()));
        }
        return vo;
    }
}
