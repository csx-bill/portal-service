package com.bill.portal.scrm.callback.strategy.impl;

import com.binarywang.spring.starter.wxjava.cp.service.WxCpTpMultiServices;
import com.bill.portal.scrm.callback.dto.SuiteWxCallbackPayload;
import com.bill.portal.scrm.callback.strategy.WxCallbackStrategy;
import com.bill.portal.scrm.core.annotation.ConditionalOnSuiteMode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.bean.message.WxCpTpXmlMessage;
import me.chanjar.weixin.cp.tp.service.WxCpTpService;
import me.chanjar.weixin.cp.util.crypto.WxCpTpCryptUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 服务商应用策略回调实现
 */
@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnSuiteMode
public class SuiteWxCallbackStrategy implements WxCallbackStrategy {

    private final WxCpTpMultiServices wxCpTpMultiServices;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public String verifyUrl(String app, String msgSignature, String timestamp, String nonce, String echostr) {
        WxCpTpService wxCpTpService = wxCpTpMultiServices.getWxCpTpService(app);
        // 使用 WxJava 内置的解密工具验证签名并解密 echostr
        WxCpTpCryptUtil cryptUtil = new WxCpTpCryptUtil(wxCpTpService.getWxCpTpConfigStorage());
        String plainText = cryptUtil.decrypt(echostr);
        log.info("服务商应用 URL 验证成功，解密后 echostr: {}", plainText);
        return plainText;
    }

    @Override
    public void decrypt(String app, String requestBody, String msgSignature, String timestamp, String nonce) {
        WxCpTpService wxCpTpService = wxCpTpMultiServices.getWxCpTpService(app);
        // 1. 解析并解密 XML 消息体
        WxCpTpXmlMessage xmlMessage = WxCpTpXmlMessage.fromEncryptedXml(requestBody,
                wxCpTpService.getWxCpTpConfigStorage(), timestamp, nonce, msgSignature);
        log.info("服务商应用 收到事件推送，app: {}, MsgType: {}, Event: {}, Content: {}",
                app,xmlMessage.getMsgType(), xmlMessage.getEvent(), xmlMessage.getContent());

        // 发布事件
        SuiteWxCallbackPayload payload = new SuiteWxCallbackPayload(app, xmlMessage);
        eventPublisher.publishEvent(payload);
    }
}
