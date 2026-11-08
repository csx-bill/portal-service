package com.bill.portal.scrm.tag.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bill.portal.scrm.tag.dto.WxCorpTagGroupQuery;
import com.bill.portal.scrm.tag.entity.WxCorpTag;
import com.bill.portal.scrm.tag.entity.WxCorpTagGroup;
import com.bill.portal.scrm.tag.mapper.WxCorpTagGroupMapper;
import com.bill.portal.scrm.tag.mapper.WxCorpTagMapper;
import com.bill.portal.scrm.tag.service.IWxCorpTagGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WxCorpTagGroupServiceImpl extends ServiceImpl<WxCorpTagGroupMapper, WxCorpTagGroup> implements IWxCorpTagGroupService {

    private final WxCorpTagMapper wxCorpTagMapper;

    @Override
    public List<WxCorpTagGroup> groupList(WxCorpTagGroupQuery query) {

        LambdaQueryWrapper<WxCorpTagGroup> groupWrapper = Wrappers.<WxCorpTagGroup>lambdaQuery()
                .eq(StringUtils.hasText(query.getCorpId()), WxCorpTagGroup::getCorpId, query.getCorpId())
                .like(StringUtils.hasText(query.getGroupName()), WxCorpTagGroup::getGroupName, query.getGroupName());

        List<WxCorpTagGroup> groupList = super.list(groupWrapper);
        if (CollUtil.isEmpty(groupList)) {
            return groupList;
        }

        // 1. 提取 groupId
        List<String> groupIds = groupList.stream()
                .map(WxCorpTagGroup::getGroupId)
                .distinct()
                .collect(Collectors.toList());

        // 2. 一次查询所有标签
        List<WxCorpTag> tagList = wxCorpTagMapper.selectList(
                Wrappers.<WxCorpTag>lambdaQuery()
                        .eq(StringUtils.hasText(query.getCorpId()),WxCorpTag::getCorpId, query.getCorpId())
                        .in(WxCorpTag::getGroupId, groupIds)
                        .orderByAsc(WxCorpTag::getOrder)
        );

        // 3. 按 groupId 分组
        Map<String, List<WxCorpTag>> tagMap = tagList.stream()
                .collect(Collectors.groupingBy(WxCorpTag::getGroupId));

        // 4. 组装结果
        for (WxCorpTagGroup group : groupList) {
            group.setTagList(tagMap.getOrDefault(group.getGroupId(), Collections.emptyList()));
        }
        return groupList;
    }

    @Override
    public WxCorpTagGroup getById(Serializable id) {
        WxCorpTagGroup wxCorpTagGroup = super.getById(id);
        List<WxCorpTag> tagList = wxCorpTagMapper.selectList(
                Wrappers.<WxCorpTag>lambdaQuery()
                        .eq(WxCorpTag::getCorpId, wxCorpTagGroup.getCorpId())
                        .eq(WxCorpTag::getGroupId, wxCorpTagGroup.getGroupId())
                        .orderByAsc(WxCorpTag::getOrder)
        );
        wxCorpTagGroup.setTagList(tagList);
        return wxCorpTagGroup;
    }
}
