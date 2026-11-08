package com.bill.portal.scrm.groupchat.service;

public interface IWxGroupChatSyncService {

    /**
     * 执行全量同步
     * @param tenantId 租户ID
     * @param corpId 企业ID
     * @param syncType 同步类型
     * @param triggerType 触发类型
     */
    void executeFullSync(Long tenantId,String corpId,String syncType, String triggerType);

    /**
     * 同步群信息（create/update 事件：重拉群详情覆盖本地）
     * @param tenantId
     * @param corpId
     * @param syncType
     * @param triggerType
     * @param chatId
     * @param updateDetail 变更详情类型（add_member/del_member/change_owner/change_name/change_notice）
     * @param lastMemVer 事件变更前群成员版本号（自建应用回调携带，用于一致性校验）
     * @param curMemVer 事件变更后群成员版本号（自建应用回调携带）
     */
    void syncGroupChat(Long tenantId,String corpId,String syncType, String triggerType,String chatId,
                       String updateDetail, Long lastMemVer, Long curMemVer);

    /**
     * 群解散标记（dismiss 事件）：群与成员记录保留，打解散标记供列表标识
     * @param tenantId
     * @param corpId
     * @param syncType
     * @param triggerType
     * @param chatId
     */
    void syncDismissGroupChat(Long tenantId,String corpId,String syncType, String triggerType,String chatId);
}
