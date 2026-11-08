package com.bill.portal.scrm.common.enums;

import lombok.Getter;

/**
 * 企微数据同步类型枚举
 * 用于 wx_sync_version 的 sync_type 字段
 */
@Getter
public enum SyncType {

    // ========== 通讯录模块 ==========
    CONTACT_FULL("CONTACT_FULL", "通讯录全量同步"),
    CONTACT_INCREMENT("CONTACT_INCREMENT", "通讯录增量同步"),

    // ========== 客户联系模块 ==========
    CUSTOMER_FULL("CUSTOMER_FULL", "客户全量同步"),
    CUSTOMER_INCREMENT("CUSTOMER_INCREMENT", "客户增量同步"),

    // ========== 客户群模块 ==========
    GROUP_CHAT_FULL("GROUP_CHAT_FULL", "客户群全量同步"),
    GROUP_CHAT_INCREMENT("GROUP_CHAT_INCREMENT", "客户群增量同步"),

    // ========== 标签模块 ==========
    TAG_FULL("TAG_FULL", "标签全量同步"),
    TAG_INCREMENT("TAG_INCREMENT", "标签增量同步"),

    // ========== 获客链接模块 ==========
    ACQUISITION_LINK_FULL("ACQUISITION_LINK_FULL", "获客链接全量同步");

    private final String code;
    private final String desc;

    SyncType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

