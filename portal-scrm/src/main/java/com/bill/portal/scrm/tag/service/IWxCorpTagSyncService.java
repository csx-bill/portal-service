package com.bill.portal.scrm.tag.service;

import com.bill.portal.scrm.tag.dto.WxTagGroupCreateReq;
import com.bill.portal.scrm.tag.dto.WxTagGroupUpdateReq;
import me.chanjar.weixin.common.error.WxErrorException;

public interface IWxCorpTagSyncService {

    /**
     * 执行全量同步
     * @param tenantId 租户ID
     * @param corpId 企业ID
     * @param syncType 同步类型
     * @param triggerType 触发类型
     */
    void executeFullSync(Long tenantId,String corpId,String syncType, String triggerType);

    void createTag(WxTagGroupCreateReq req) throws WxErrorException;

    void updateTag(WxTagGroupUpdateReq req) throws WxErrorException;
    void delGroup(Long id) throws WxErrorException;
}
