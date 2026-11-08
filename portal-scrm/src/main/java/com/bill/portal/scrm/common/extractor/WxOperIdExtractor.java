package com.bill.portal.scrm.common.extractor;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.bean.message.WxCpTpXmlMessage;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import me.chanjar.weixin.cp.constant.WxCpConsts;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class WxOperIdExtractor {

    /**
     * 根据事件类型，从原始 XML 报文中提取操作对象的主键 ID (operId)
     */
    public String extract(Object rawMessage) {
        if (rawMessage == null) {
            return null;
        }

        // 服务商应用报文提取
        if (rawMessage instanceof WxCpTpXmlMessage) {
            WxCpTpXmlMessage msg = (WxCpTpXmlMessage) rawMessage;
            return extractFromTpMsg(msg);
        }

        // 自建应用报文提取
        if (rawMessage instanceof WxCpXmlMessage) {
            WxCpXmlMessage msg = (WxCpXmlMessage) rawMessage;
            return extractFromSelfMsg(msg);
        }

        log.warn("未知的报文类型，无法提取 operId: {}", rawMessage.getClass().getName());
        return null;
    }

    /**
     * 服务商模式提取逻辑
     */
    private String extractFromTpMsg(WxCpTpXmlMessage msg) {
        switch (msg.getEvent()) {
            case WxCpConsts.EventType.CHANGE_EXTERNAL_CONTACT:
                return msg.getExternalUserID();
            case WxCpConsts.EventType.CHANGE_EXTERNAL_CHAT:
                return msg.getChatId();
            default:
                return null;
        }
    }

    /**
     * 自建应用模式提取逻辑
     */
    private String extractFromSelfMsg(WxCpXmlMessage msg) {
        switch (msg.getEvent()) {
            case WxCpConsts.EventType.CHANGE_EXTERNAL_CONTACT:
                return msg.getExternalUserId();
            case WxCpConsts.EventType.CHANGE_EXTERNAL_CHAT:
                return msg.getChatId();
            default:
                return null;
        }
    }
}
