package com.bill.portal.scrm.callback.handler;

import com.bill.portal.common.core.datascope.DataScopeBroker;
import com.bill.portal.scrm.callback.handler.impl.ChangeExternalChatHandler;
import com.bill.portal.scrm.callback.handler.impl.ChangeExternalContactHandler;
import com.bill.portal.scrm.callback.handler.impl.ChangeExternalTagHandler;
import com.bill.portal.scrm.common.enums.EventType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class WxEventDispatcher {

    private final Map<EventType, WxEventHandler> handlerMap;

    public WxEventDispatcher(List<WxEventHandler> handlers) {
        handlerMap = new EnumMap<>(EventType.class);

        handlerMap.put(EventType.CHANGE_CONTACT,
                handlers.stream()
                        .filter(h -> h instanceof ChangeExternalContactHandler)
                        .findFirst().get());


        handlerMap.put(EventType.CHANGE_EXTERNAL_CONTACT,
                handlers.stream()
                        .filter(h -> h instanceof ChangeExternalContactHandler)
                        .findFirst().get());

        handlerMap.put(EventType.CHANGE_EXTERNAL_CHAT,
                handlers.stream()
                        .filter(h -> h instanceof ChangeExternalChatHandler)
                        .findFirst().get());

        handlerMap.put(EventType.CHANGE_EXTERNAL_TAG,
                handlers.stream()
                        .filter(h -> h instanceof ChangeExternalTagHandler)
                        .findFirst().get());
    }

    public void dispatch(String eventType, Object payload) {
        EventType type = EventType.of(eventType);
        WxEventHandler handler = handlerMap.get(type);
        // 回调线程无登录上下文，userId 为空时数据权限按拒绝处理（fail-closed）；
        // 回调属系统级操作，本次处理全量放行
        DataScopeBroker.skip(() -> handler.handle(payload));
    }
}
