package com.bill.portal.scrm.tag.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bill.portal.scrm.common.service.ISyncVersionService;
import com.bill.portal.scrm.corp.strategy.WxCorpApiStrategy;
import com.bill.portal.scrm.tag.dto.WxTagCreateReq;
import com.bill.portal.scrm.tag.dto.WxTagGroupCreateReq;
import com.bill.portal.scrm.tag.dto.WxTagGroupUpdateReq;
import com.bill.portal.scrm.tag.entity.WxCorpTag;
import com.bill.portal.scrm.tag.entity.WxCorpTagGroup;
import com.bill.portal.scrm.tag.mapper.WxCorpTagGroupMapper;
import com.bill.portal.scrm.tag.mapper.WxCorpTagMapper;
import com.bill.portal.scrm.tag.service.IWxCorpTagSyncService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.WxCpExternalContactService;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.external.WxCpUserExternalTagGroupInfo;
import me.chanjar.weixin.cp.bean.external.WxCpUserExternalTagGroupList;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 企微客户标签同步核心服务
 */
@Slf4j
@Service
@AllArgsConstructor
public class WxCorpTagSyncServiceImpl implements IWxCorpTagSyncService {

    private final WxCorpApiStrategy wxCorpApiStrategy;

    private final WxCorpTagMapper wxCorpTagMapper;

    private final WxCorpTagGroupMapper wxCorpTagGroupMapper;

    private final ISyncVersionService versionService;

    /**
     * 执行全量同步
     * @param tenantId 租户ID
     * @param corpId 企业ID
     * @param syncType 同步类型
     * @param triggerType 触发类型
     */
    @Override
    public void executeFullSync(Long tenantId,String corpId,String syncType, String triggerType) {
        Long currentVersion = versionService.generateNewVersion(corpId, syncType, triggerType);
        try {
            WxCpService wxCpService = wxCorpApiStrategy.getWxCpService(corpId);
            // 拉取企微全量标签组列表
            WxCpExternalContactService externalContactService = wxCpService.getExternalContactService();
            WxCpUserExternalTagGroupList corpTagList = externalContactService.getCorpTagList(null);

            List<WxCpUserExternalTagGroupList.TagGroup> tagGroupList = corpTagList.getTagGroupList();

            // 执行标签组和标签的同步逻辑
            syncCorpTagGroups(tenantId,corpId, tagGroupList, currentVersion);

            versionService.updateVersionStatus(corpId, syncType, currentVersion, "SUCCESS", null);
            log.info("企业[{}]全量同步完成, version: {}", corpId, currentVersion);
        } catch (Exception e) {
            log.error("企业[{}]全量同步失败, version: {}", corpId, currentVersion, e);
            versionService.updateVersionStatus(corpId, syncType, currentVersion, "FAIL", e.getMessage());
        }
    }

    @Override
    public void createTag(WxTagGroupCreateReq req) throws WxErrorException {
        // 获取外部联系人服务
        WxCpExternalContactService externalContactService =
                wxCorpApiStrategy.getWxCpService(req.getCorpId()).getExternalContactService();

        // 构建标签组
        WxCpUserExternalTagGroupInfo.TagGroup tagGroup = new WxCpUserExternalTagGroupInfo.TagGroup();
        tagGroup.setGroupId(req.getGroupId());
        tagGroup.setGroupName(req.getGroupName());
        tagGroup.setOrder(req.getOrder());
        tagGroup.setTag(
                Optional.ofNullable(req.getTagList())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(tagReq -> {
                            WxCpUserExternalTagGroupInfo.Tag tag = new WxCpUserExternalTagGroupInfo.Tag();
                            tag.setName(tagReq.getTagName());
                            tag.setOrder(tagReq.getOrder());
                            return tag;
                        })
                        .collect(Collectors.toList())
        );

        WxCpUserExternalTagGroupInfo tagGroupInfo = new WxCpUserExternalTagGroupInfo();
        tagGroupInfo.setTagGroup(tagGroup);

        // 调用企业微信接口
        WxCpUserExternalTagGroupInfo result = externalContactService.addCorpTag(tagGroupInfo);
        WxCpUserExternalTagGroupInfo.TagGroup resultGroup = result.getTagGroup();
        String groupId = resultGroup.getGroupId();

        // 保存标签组
        WxCorpTagGroup wxCorpTagGroup = new WxCorpTagGroup();
        wxCorpTagGroup.setCorpId(req.getCorpId());
        wxCorpTagGroup.setGroupId(groupId);
        wxCorpTagGroup.setGroupName(req.getGroupName());
        wxCorpTagGroup.setOrder(req.getOrder());
        wxCorpTagGroup.setSyncVersion(1L);
        wxCorpTagGroupMapper.insert(wxCorpTagGroup);

        // 保存标签列表
        List<WxCorpTag> wxCorpTags =
                Optional.ofNullable(resultGroup.getTag())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(tag -> {
                            WxCorpTag corpTag = new WxCorpTag();
                            corpTag.setCorpId(req.getCorpId());
                            corpTag.setGroupId(groupId);
                            corpTag.setTagId(tag.getId());
                            corpTag.setTagName(tag.getName());
                            corpTag.setOrder(tag.getOrder());
                            corpTag.setSyncVersion(1L);
                            return corpTag;
                        })
                        .collect(Collectors.toList());

        if (!wxCorpTags.isEmpty()) {
            wxCorpTagMapper.insert(wxCorpTags);
        }
    }

    @Override
    public void updateTag(WxTagGroupUpdateReq req) throws WxErrorException {
        WxCpExternalContactService externalContactService =
                wxCorpApiStrategy.getWxCpService(req.getCorpId()).getExternalContactService();

        // ===================== 1. 标签组变更检测 =====================
        WxCorpTagGroup oldGroup = wxCorpTagGroupMapper.selectById(req.getId());

        boolean groupChanged = oldGroup == null ||
                !Objects.equals(oldGroup.getGroupName(), req.getGroupName()) ||
                !Objects.equals(oldGroup.getOrder(), req.getOrder());

        if (groupChanged) {
            externalContactService.editCorpTag(
                    req.getGroupId(),
                    req.getGroupName(),
                    req.getOrder().intValue()
            );

            WxCorpTagGroup updateGroup = new WxCorpTagGroup();
            updateGroup.setId(req.getId());
            updateGroup.setGroupName(req.getGroupName());
            updateGroup.setOrder(req.getOrder());
            wxCorpTagGroupMapper.updateById(updateGroup);
        }

        // ===================== 2. 数据库已有标签 =====================
        List<WxCorpTag> dbTags = wxCorpTagMapper.selectList(
                Wrappers.<WxCorpTag>lambdaQuery()
                        .eq(WxCorpTag::getGroupId, req.getGroupId())
                        .eq(WxCorpTag::getCorpId, req.getCorpId())
        );

        Map<Long, WxCorpTag> dbTagMap = dbTags.stream()
                .collect(Collectors.toMap(WxCorpTag::getId, Function.identity()));

        List<Long> reqTagIds = new ArrayList<>();
        List<WxTagCreateReq> toInsert = new ArrayList<>();
        List<WxCorpTag> toUpdate = new ArrayList<>();

        // ===================== 3. 标签增 / 改拆分 =====================
        Optional.ofNullable(req.getTagList()).orElse(Collections.emptyList())
                .forEach(tagReq -> {
                    if (tagReq.getId() == null) {
                        WxTagCreateReq insert = new WxTagCreateReq();
                        insert.setTagName(tagReq.getTagName());
                        insert.setOrder(tagReq.getOrder());
                        toInsert.add(insert);
                    } else {
                        reqTagIds.add(tagReq.getId());

                        WxCorpTag dbTag = dbTagMap.get(tagReq.getId());
                        if (dbTag == null) {
                            return;
                        }

                        boolean tagChanged =
                                !Objects.equals(dbTag.getTagName(), tagReq.getTagName())
                                        || !Objects.equals(dbTag.getOrder(), tagReq.getOrder());

                        if (tagChanged) {
                            WxCorpTag update = new WxCorpTag();
                            update.setId(tagReq.getId());
                            update.setTagId(dbTag.getTagId());
                            update.setTagName(tagReq.getTagName());
                            update.setOrder(tagReq.getOrder());
                            toUpdate.add(update);
                        }
                    }
                });

        // ===================== 4. 新增标签（批量） =====================
        if (!toInsert.isEmpty()) {
            WxTagGroupCreateReq createReq = new WxTagGroupCreateReq();
            createReq.setCorpId(req.getCorpId());
            createReq.setGroupId(req.getGroupId());
            createReq.setTagList(toInsert);
            this.createTag(createReq);
        }

        // ===================== 5. 更新标签（批量 edit） =====================
        if (!toUpdate.isEmpty()) {
            for (WxCorpTag wxCorpTag : toUpdate) {
                externalContactService.editCorpTag(
                        wxCorpTag.getTagId(),
                        wxCorpTag.getTagName(),
                        wxCorpTag.getOrder().intValue()
                );
            }
            wxCorpTagMapper.updateById(toUpdate);
        }

        // ===================== 6. 删除标签（差集驱动） =====================
        List<Long> toDeleteIds = dbTagMap.keySet().stream()
                .filter(id -> !reqTagIds.contains(id))
                .collect(Collectors.toList());

        if (!toDeleteIds.isEmpty()) {
            List<String> wxTagIds = toDeleteIds.stream()
                    .map(dbTagMap::get)
                    .filter(Objects::nonNull)
                    .map(WxCorpTag::getTagId)
                    .collect(Collectors.toList());

            if (!wxTagIds.isEmpty()) {
                externalContactService.delCorpTag(wxTagIds.toArray(new String[0]), null);
                wxCorpTagMapper.deleteBatchIds(toDeleteIds);
            }
        }

        // ===================== 7. 标签组为空 → 级联删除标签组 =====================
        long remainTagCount = wxCorpTagMapper.selectCount(
                Wrappers.<WxCorpTag>lambdaQuery()
                        .eq(WxCorpTag::getGroupId, req.getGroupId())
                        .eq(WxCorpTag::getCorpId, req.getCorpId())
        );

        if (remainTagCount == 0) {
            // 删除本地标签组
            wxCorpTagGroupMapper.deleteById(req.getId());
        }
    }

    @Override
    public void delGroup(Long id) throws WxErrorException {
        WxCorpTagGroup wxCorpTagGroup = wxCorpTagGroupMapper.selectById(id);
        WxCpExternalContactService externalContactService =
                wxCorpApiStrategy.getWxCpService(wxCorpTagGroup.getCorpId()).getExternalContactService();

        externalContactService.delCorpTag(null, new String[]{wxCorpTagGroup.getGroupId()});
        wxCorpTagGroupMapper.deleteById(id);
        wxCorpTagMapper.delete(Wrappers.<WxCorpTag>lambdaQuery().eq(WxCorpTag::getCorpId,wxCorpTagGroup.getCorpId()).eq(WxCorpTag::getGroupId,wxCorpTagGroup.getCorpId()));
    }


    /**
     * 批量同步标签组
     */
    private void syncCorpTagGroups(Long tenantId,String corpId, List<WxCpUserExternalTagGroupList.TagGroup> tagGroupList, Long version) {
        if (tagGroupList == null || tagGroupList.isEmpty()) return;

        List<WxCorpTagGroup> list = tagGroupList.stream().map(dto -> {
            WxCorpTagGroup tagGroup = BeanUtil.copyProperties(dto, WxCorpTagGroup.class);
            tagGroup.setId(null);
            tagGroup.setCorpId(corpId);
            tagGroup.setSyncVersion(version);
            // 将企微返回的完整 DTO 转为 Map 存入快照字段
            tagGroup.setExtraData(BeanUtil.beanToMap(dto));
            tagGroup.setTenantId(tenantId);
            tagGroup.setCreateTime(LocalDateTime.now());
            tagGroup.setUpdateTime(LocalDateTime.now());
            tagGroup.setDelFlag(Boolean.TRUE.equals(dto.getDeleted()) ? 1 : 0);

            // 同步标签
            syncCorpTags(tenantId,corpId, dto.getTag(), version,dto.getGroupId());

            return tagGroup;
        }).collect(Collectors.toList());

        // 批量 Upsert
        wxCorpTagGroupMapper.batchUpsert(list);
    }


    /**
     * 批量同步标签
     */
    private void syncCorpTags(Long tenantId, String corpId, List<WxCpUserExternalTagGroupList.TagGroup.Tag> tagList, Long version,String groupId) {
        if (tagList == null || tagList.isEmpty()) return;

        List<WxCorpTag> list = tagList.stream().map(dto -> {
            WxCorpTag tag = new WxCorpTag();
            tag.setGroupId(groupId);
            tag.setTagId(dto.getId());
            tag.setTagName(dto.getName());
            tag.setOrder(dto.getOrder());
            tag.setCorpId(corpId);
            tag.setSyncVersion(version);
            tag.setTenantId(tenantId);
            tag.setCreateTime(LocalDateTime.now());
            tag.setUpdateTime(LocalDateTime.now());
            tag.setDelFlag(Boolean.TRUE.equals(dto.getDeleted()) ? 1 : 0);
            return tag;
        }).collect(Collectors.toList());

        // 批量 Upsert
        wxCorpTagMapper.batchUpsert(list);
    }

}
