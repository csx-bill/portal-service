package com.bill.portal.scrm.corp.strategy;

import me.chanjar.weixin.cp.api.WxCpService;


/**
 * 企微客户(外部联系人) API 策略接口
 * 作用：在业务层屏蔽底层 API 的调用差异 (自建应用 vs 服务商)
 */
public interface WxCorpApiStrategy {
    
    /**
     * @param corpId 企业ID
     * @return
     */
    WxCpService getWxCpService(String corpId);

}
