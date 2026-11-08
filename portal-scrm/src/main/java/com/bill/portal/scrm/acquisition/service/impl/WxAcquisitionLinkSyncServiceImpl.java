package com.bill.portal.scrm.acquisition.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.bill.portal.common.core.exception.CheckedException;
import com.bill.portal.scrm.acquisition.dto.WxAcquisitionLinkCreateReq;
import com.bill.portal.scrm.acquisition.dto.WxAcquisitionLinkUpdateReq;
import com.bill.portal.scrm.acquisition.dto.WxCpCustomerAcquisitionExInfo;
import com.bill.portal.scrm.acquisition.dto.WxCpCustomerAcquisitionExRequest;
import com.bill.portal.scrm.acquisition.entity.WxAcquisitionLink;
import com.bill.portal.scrm.acquisition.mapper.WxAcquisitionLinkMapper;
import com.bill.portal.scrm.acquisition.service.IWxAcquisitionLinkSyncService;
import com.bill.portal.scrm.common.service.ISyncVersionService;
import com.bill.portal.scrm.corp.strategy.WxCorpApiStrategy;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.WxCpExternalContactService;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.external.acquisition.WxCpCustomerAcquisitionCreateResult;
import me.chanjar.weixin.cp.bean.external.acquisition.WxCpCustomerAcquisitionInfo;
import me.chanjar.weixin.cp.bean.external.acquisition.WxCpCustomerAcquisitionList;
import me.chanjar.weixin.cp.util.json.WxCpGsonBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 获客链接同步核心服务
 */
@Slf4j
@Service
@AllArgsConstructor
public class WxAcquisitionLinkSyncServiceImpl implements IWxAcquisitionLinkSyncService {

    /** 详情解析用：SDK Info 未映射 priority_option/mark_source，用扩展子类保留完整配置 */
    private static final Gson GSON = WxCpGsonBuilder.create();

    private static final String DETAIL_URL = "/cgi-bin/externalcontact/customer_acquisition/get";

    private final WxCorpApiStrategy wxCorpApiStrategy;
    private final WxAcquisitionLinkMapper wxAcquisitionLinkMapper;
    private final ISyncVersionService versionService;

    @Override
    public void executeFullSync(Long tenantId, String corpId, String syncType, String triggerType) {
        Long currentVersion = versionService.generateNewVersion(corpId, syncType, triggerType);
        try {
            WxCpService wxCpService = wxCorpApiStrategy.getWxCpService(corpId);
            WxCpExternalContactService externalContactService = wxCpService.getExternalContactService();

            List<WxAcquisitionLink> allLinks = new ArrayList<>();
            String cursor = null;
            do {
                // 调用企微 API
                WxCpCustomerAcquisitionList wxCpCustomerAcquisitionList = externalContactService.customerAcquisitionLinkList(100, cursor);

                List<String> linkIdList = wxCpCustomerAcquisitionList.getLinkIdList();
                cursor =wxCpCustomerAcquisitionList.getNextCursor();

                if (CollUtil.isNotEmpty(linkIdList)) {
                    for (String linkId : linkIdList) {
                        WxAcquisitionLink link = fetchLinkDetail(wxCpService, tenantId, corpId, linkId, currentVersion);
                        if (link != null) {
                            allLinks.add(link);
                        }
                    }
                }
            } while (StrUtil.isNotBlank(cursor));

            // 批量 Upsert
            if (CollUtil.isNotEmpty(allLinks)) {
                wxAcquisitionLinkMapper.batchUpsert(allLinks);
            }

            versionService.updateVersionStatus(corpId, syncType, currentVersion, "SUCCESS", null);
            log.info("企业[{}]获客链接全量同步完成, version: {}, 数量: {}", corpId, currentVersion, allLinks.size());
        } catch (Exception e) {
            log.error("企业[{}]获客链接全量同步失败, version: {}", corpId, currentVersion, e);
            versionService.updateVersionStatus(corpId, syncType, currentVersion, "FAIL", e.getMessage());
        }
    }

    @Override
    public void createLink(WxAcquisitionLinkCreateReq req) throws WxErrorException {
        WxCpService wxCpService = wxCorpApiStrategy.getWxCpService(req.getCorpId());
        WxCpExternalContactService externalContactService = wxCpService.getExternalContactService();

        // 调用企微 API（扩展请求支持 priority_option / mark_source）
        WxCpCustomerAcquisitionExRequest acquisitionRequest = new WxCpCustomerAcquisitionExRequest();
        acquisitionRequest.setLinkName(req.getLinkName());
        acquisitionRequest.setSkipVerify(req.getSkipVerify());

        // range
        if (CollUtil.isNotEmpty(req.getRangeUserList()) || CollUtil.isNotEmpty(req.getRangeDepartmentList())) {
            WxCpCustomerAcquisitionInfo.Range range = new WxCpCustomerAcquisitionInfo.Range();
            range.setUserList( req.getRangeUserList());
            range.setDepartmentList(req.getRangeDepartmentList()
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList()));
            acquisitionRequest.setRange(range);
        }
        applyPriorityAndSource(acquisitionRequest,
                req.getPriorityType(), req.getPriorityUseridList(), req.getMarkSource());

        WxCpCustomerAcquisitionCreateResult wxCpCustomerAcquisitionCreateResult = externalContactService.customerAcquisitionLinkCreate(acquisitionRequest);
        WxCpCustomerAcquisitionInfo.Link link = wxCpCustomerAcquisitionCreateResult.getLink();

        // 持久化到本地
        WxAcquisitionLink entity = BeanUtil.copyProperties(req, WxAcquisitionLink.class);
        entity.setLinkId(link.getLinkId());
        entity.setUrl(link.getUrl());
        wxAcquisitionLinkMapper.insert(entity);
    }

    @Override
    public void updateLink(WxAcquisitionLinkUpdateReq req) throws WxErrorException {
        WxCpService wxCpService = wxCorpApiStrategy.getWxCpService(req.getCorpId());
        WxCpExternalContactService externalContactService = wxCpService.getExternalContactService();

        // 调用企微 API（扩展请求支持 priority_option / mark_source）
        WxCpCustomerAcquisitionExRequest acquisitionRequest = new WxCpCustomerAcquisitionExRequest();
        acquisitionRequest.setLinkId(req.getLinkId());
        acquisitionRequest.setLinkName(req.getLinkName());
        acquisitionRequest.setSkipVerify(req.getSkipVerify());

        // range
        if (CollUtil.isNotEmpty(req.getRangeUserList()) || CollUtil.isNotEmpty(req.getRangeDepartmentList())) {
            WxCpCustomerAcquisitionInfo.Range range = new WxCpCustomerAcquisitionInfo.Range();
            range.setUserList( req.getRangeUserList());
            range.setDepartmentList(req.getRangeDepartmentList()
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList()));
            acquisitionRequest.setRange(range);
        }
        applyPriorityAndSource(acquisitionRequest,
                req.getPriorityType(), req.getPriorityUseridList(), req.getMarkSource());

        externalContactService.customerAcquisitionUpdate(acquisitionRequest);

        // 更新本地记录
        WxAcquisitionLink entity = BeanUtil.copyProperties(req, WxAcquisitionLink.class);
        wxAcquisitionLinkMapper.updateById(entity);
    }

    @Override
    public void deleteLink(Long id) throws WxErrorException {
        WxAcquisitionLink wxAcquisitionLink = wxAcquisitionLinkMapper.selectById(id);
        WxCpService wxCpService = wxCorpApiStrategy.getWxCpService(wxAcquisitionLink.getCorpId());
        WxCpExternalContactService externalContactService = wxCpService.getExternalContactService();
        // 调用企微 API
        externalContactService.customerAcquisitionLinkDelete(wxAcquisitionLink.getLinkId());
        wxAcquisitionLinkMapper.deleteById(id);
    }

    @Override
    public void batchDeleteLink(String ids) throws WxErrorException {
        List<String> list = Arrays.asList(ids.split(","));
        for (String id : list) {
            this.deleteLink(Long.parseLong(id));
        }
    }

    /**
     * 获取单个链接详情（直接调 customer_acquisition/get 并用扩展类解析，
     * 保留 SDK Info 未映射的 priority_option / mark_source）
     */
    private WxAcquisitionLink fetchLinkDetail(WxCpService wxCpService, Long tenantId, String corpId, String linkId, Long version) throws WxErrorException {
        JsonObject body = new JsonObject();
        body.addProperty("link_id", linkId);
        String url = wxCpService.getWxCpConfigStorage().getApiUrl(DETAIL_URL);
        WxCpCustomerAcquisitionExInfo info = GSON.fromJson(
                wxCpService.post(url, body), WxCpCustomerAcquisitionExInfo.class);
        WxCpCustomerAcquisitionInfo.Link link = info.getLink();

        WxAcquisitionLink entity = new WxAcquisitionLink();
        entity.setCorpId(corpId);
        entity.setTenantId(tenantId);
        entity.setSyncVersion(version);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        entity.setLinkId(link.getLinkId());
        entity.setLinkName(link.getLinkName());
        entity.setUrl(link.getUrl());
        entity.setSkipVerify(info.getSkipVerify());
        entity.setMarkSource(info.getMarkSource());
        WxCpCustomerAcquisitionInfo.Range range = info.getRange();

        if (range != null) {
            entity.setRangeUserList(range.getUserList());
            entity.setRangeDepartmentList(range.getDepartmentList().stream()
                    .map(Integer::valueOf)
                    .collect(Collectors.toList()));
        }
        if (info.getPriorityOption() != null) {
            entity.setPriorityType(info.getPriorityOption().getPriorityType());
            entity.setPriorityUseridList(info.getPriorityOption().getPriorityUseridList());
        }
        return entity;
    }

    /**
     * 组装 priority_option / mark_source（官方：priority_type=2 时成员列表必填
     * 且最多1000个；不设置时不传对应字段，保持企微侧原值）
     */
    private void applyPriorityAndSource(WxCpCustomerAcquisitionExRequest request,
                                        Integer priorityType, List<String> priorityUseridList,
                                        Boolean markSource) {
        if (priorityType != null) {
            if (priorityType != 1 && priorityType != 2) {
                throw new CheckedException("priority_type 仅支持 1 或 2");
            }
            if (priorityType == 2 && CollUtil.isEmpty(priorityUseridList)) {
                throw new CheckedException("priority_type=2 时必须指定优先成员列表");
            }
            if (CollUtil.isNotEmpty(priorityUseridList) && priorityUseridList.size() > 1000) {
                throw new CheckedException("优先成员列表最多1000个");
            }
            WxCpCustomerAcquisitionExRequest.PriorityOption option =
                    new WxCpCustomerAcquisitionExRequest.PriorityOption();
            option.setPriorityType(priorityType);
            option.setPriorityUseridList(priorityUseridList);
            request.setPriorityOption(option);
        }
        if (markSource != null) {
            request.setMarkSource(markSource);
        }
    }

}
