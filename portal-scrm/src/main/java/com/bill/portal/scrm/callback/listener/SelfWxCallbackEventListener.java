package com.bill.portal.scrm.callback.listener;

import com.bill.portal.scrm.callback.dto.SelfWxCallbackPayload;
import com.bill.portal.scrm.callback.handler.WxEventDispatcher;
import com.bill.portal.scrm.common.enums.EventType;
import com.bill.portal.scrm.core.annotation.ConditionalOnSelfMode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnSelfMode
public class SelfWxCallbackEventListener {

    private final WxEventDispatcher dispatcher;


    @Async
    @EventListener
    public void onSelfCallbackEvent(SelfWxCallbackPayload payload) {
        String app = payload.getApp();
        WxCpXmlMessage xmlMessage = payload.getXmlMessage();

        // 1. 根据枚举判断是否需要处理该事件
        if (!EventType.isHandleEvent(xmlMessage.getEvent())) {
            log.info("【内部应用】忽略不处理的事件类型: app={}, eventType={}", app, xmlMessage.getEvent());
            return;
        }

        dispatcher.dispatch(xmlMessage.getEvent(),payload);

    }
}
