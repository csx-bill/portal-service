package com.bill.portal.scrm.common.enums;

import lombok.Getter;

/**
 * 企微数据同步触发类型枚举
 * 用于 wx_sync_version 的 trigger_type 字段
 */
@Getter
public enum TriggerType {

    CRON("CRON", "定时任务触发"),
    MANUAL("MANUAL", "手动触发"),
    CALLBACK("CALLBACK", "企微回调触发"),
    SYSTEM("SYSTEM", "系统内部触发(如初始化)");

    private final String code;
    private final String desc;

    TriggerType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

