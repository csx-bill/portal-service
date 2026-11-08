package com.bill.portal.scrm.acquisition.service;

import com.bill.portal.scrm.acquisition.dto.WxAcquisitionLinkCreateReq;
import com.bill.portal.scrm.acquisition.dto.WxAcquisitionLinkUpdateReq;
import me.chanjar.weixin.common.error.WxErrorException;

public interface IWxAcquisitionLinkSyncService {

    /**
     * 执行全量同步
     * @param tenantId 租户ID
     * @param corpId 企业ID
     * @param syncType 同步类型
     * @param triggerType 触发类型
     */
    void executeFullSync(Long tenantId, String corpId, String syncType, String triggerType);

    /**
     * 创建获客链接
     */
    void createLink(WxAcquisitionLinkCreateReq req) throws WxErrorException;

    /**
     * 更新获客链接
     */
    void updateLink(WxAcquisitionLinkUpdateReq req) throws WxErrorException;

    /**
     * 删除获客链接
     */
    void deleteLink(Long id) throws WxErrorException;

    /**
     * 批量删除获客链接
     */
    void batchDeleteLink(String ids) throws WxErrorException;
}
