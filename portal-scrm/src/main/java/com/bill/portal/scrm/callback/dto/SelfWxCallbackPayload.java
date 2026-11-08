package com.bill.portal.scrm.callback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelfWxCallbackPayload {
    /** 应用标识 */
    private String app;
    /** 企微推送的原始 XML 报文 */
    private WxCpXmlMessage xmlMessage;
}
