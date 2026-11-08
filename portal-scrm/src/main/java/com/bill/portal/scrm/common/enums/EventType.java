package com.bill.portal.scrm.common.enums;

import lombok.Getter;
import me.chanjar.weixin.cp.constant.WxCpConsts;

/**
 * 企微事件类型枚举
 */
@Getter
public enum EventType {

    CHANGE_CONTACT(WxCpConsts.EventType.CHANGE_CONTACT, "成员变更通知"),
    CHANGE_EXTERNAL_CONTACT(WxCpConsts.EventType.CHANGE_EXTERNAL_CONTACT, "外部联系人变更"),
    CHANGE_EXTERNAL_CHAT(WxCpConsts.EventType.CHANGE_EXTERNAL_CHAT,"外部群变更"),
    CHANGE_EXTERNAL_TAG(WxCpConsts.EventType.CHANGE_EXTERNAL_TAG, "企业客户标签变更");

    private final String code;
    private final String desc;

    EventType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 核心方法：判断企微传来的 eventType 是否在我们要处理的枚举列表中
     */
    public static boolean isHandleEvent(String eventType) {
        if (eventType == null) return false;
        for (EventType value : values()) {
            if (value.getCode().equals(eventType)) {
                return true;
            }
        }
        return false;
    }

    public static EventType of(String code) {
        for (EventType e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("未知事件类型");
    }
}

