package com.bill.portal.scrm.contact.service;

public interface IWxContactSyncService {

    /**
     * 执行全量同步
     * @param tenantId 租户ID
     * @param corpId 企业ID
     * @param syncType 同步类型
     * @param triggerType 触发类型
     */
    void executeFullSync(Long tenantId,String corpId,String syncType, String triggerType);

    /**
     * 同步成员信息
     * @param tenantId 租户ID
     * @param corpId
     * @param syncType 同步类型
     * @param triggerType 触发类型
     * @param userId 用户ID
     */
    void syncUser(Long tenantId,String corpId, String syncType, String triggerType, String userId);

    /**
     * 删除成员信息
     * @param tenantId 租户ID
     * @param corpId
     * @param syncType 同步类型
     * @param triggerType 触发类型
     * @param userId 用户ID
     */
    void syncDelUser(Long tenantId,String corpId, String syncType, String triggerType, String userId);

    /**
     * 同步部门信息
     * @param tenantId 租户ID
     * @param corpId
     * @param syncType 同步类型
     * @param triggerType 触发类型
     * @param deptId 部门ID
     */
    void syncDept(Long tenantId,String corpId, String syncType, String triggerType, Long deptId);

    /**
     * 删除部门信息
     * @param tenantId 租户ID
     * @param corpId
     * @param syncType 同步类型
     * @param triggerType 触发类型
     * @param deptId 部门ID
     */
    void syncDelDept(Long tenantId,String corpId, String syncType, String triggerType, Long deptId);

}
