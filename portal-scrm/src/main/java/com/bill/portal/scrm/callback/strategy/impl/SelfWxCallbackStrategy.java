package com.bill.portal.scrm.callback.strategy.impl;

import com.binarywang.spring.starter.wxjava.cp.service.WxCpMultiServices;
import com.bill.portal.scrm.callback.dto.SelfWxCallbackPayload;
import com.bill.portal.scrm.callback.strategy.WxCallbackStrategy;
import com.bill.portal.scrm.core.annotation.ConditionalOnSelfMode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import me.chanjar.weixin.cp.util.crypto.WxCpCryptUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 自建应用策略回调实现
 */
@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnSelfMode
public class SelfWxCallbackStrategy implements WxCallbackStrategy {

    private final WxCpMultiServices wxCpMultiServices;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public String verifyUrl(String app, String msgSignature, String timestamp, String nonce, String echostr) {
        WxCpService wxCpService = wxCpMultiServices.getWxCpService(app);
        // 使用 WxJava 内置的解密工具验证签名并解密 echostr
        WxCpCryptUtil cryptUtil = new WxCpCryptUtil(wxCpService.getWxCpConfigStorage());
        String plainText = cryptUtil.decrypt(echostr);
        log.info("自建应用 URL 验证成功，解密后 echostr: {}", plainText);
        return plainText;
    }

    @Override
    public void decrypt(String app, String requestBody, String msgSignature, String timestamp, String nonce) {
        WxCpService wxCpService = wxCpMultiServices.getWxCpService(app);
        // 1. 解析并解密 XML 消息体
        WxCpXmlMessage xmlMessage = WxCpXmlMessage.fromEncryptedXml(requestBody,
                wxCpService.getWxCpConfigStorage(), timestamp, nonce, msgSignature);
        log.info("自建应用 收到事件推送，app: {}, MsgType: {}, Event: {}, Content: {}",
                app,xmlMessage.getMsgType(), xmlMessage.getEvent(), xmlMessage.getContent());

        // 发布事件
        SelfWxCallbackPayload payload = new SelfWxCallbackPayload(app, xmlMessage);
        eventPublisher.publishEvent(payload);

    }
}
