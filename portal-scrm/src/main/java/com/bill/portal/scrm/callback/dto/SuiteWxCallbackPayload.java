package com.bill.portal.scrm.callback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.chanjar.weixin.cp.bean.message.WxCpTpXmlMessage;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuiteWxCallbackPayload {
    /** 应用标识 */
    private String app;
    /** 企微推送的原始 XML 报文 */
    private WxCpTpXmlMessage xmlMessage;
}
