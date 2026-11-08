package com.bill.portal.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bill.portal.system.entity.SysFile;
import com.bill.portal.system.entity.SysFileGroup;
import com.bill.portal.system.mapper.SysFileGroupMapper;
import com.bill.portal.system.mapper.SysFileMapper;
import com.bill.portal.system.service.ISysFileGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysFileGroupServiceImpl extends ServiceImpl<SysFileGroupMapper, SysFileGroup> implements ISysFileGroupService {

    private final SysFileMapper sysFileMapper;

    @Override
    public List<SysFileGroup> getGroupTree(String clientId) {
        List<SysFileGroup> all = list(new LambdaQueryWrapper<SysFileGroup>()
                .eq(SysFileGroup::getClientId, clientId)
                .orderByAsc(SysFileGroup::getName));
        List<SysFileGroup> roots = all.stream()
                .filter(g -> g.getParentId() == null || g.getParentId() == 0)
                .collect(Collectors.toList());
        for (SysFileGroup root : roots) {
            buildChildren(root, all);
        }
        return roots;
    }

    private void buildChildren(SysFileGroup parent, List<SysFileGroup> all) {
        List<SysFileGroup> children = all.stream()
                .filter(g -> parent.getId().equals(g.getParentId()))
                .collect(Collectors.toList());
        parent.setChildren(children);
        for (SysFileGroup child : children) {
            buildChildren(child, all);
        }
    }

    @Override
    public boolean hasChildren(Long groupId) {
        Long count = lambdaQuery().eq(SysFileGroup::getParentId, groupId).count();
        return count != null && count > 0;
    }

    @Override
    public boolean hasFiles(Long groupId) {
        Long count = sysFileMapper.selectCount(new LambdaQueryWrapper<SysFile>()
                .eq(SysFile::getGroupId, groupId));
        return count != null && count > 0;
    }
}
