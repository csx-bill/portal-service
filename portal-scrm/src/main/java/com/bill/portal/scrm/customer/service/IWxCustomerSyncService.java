package com.bill.portal.scrm.customer.service;

import com.bill.portal.scrm.customer.dto.WxCustomerMarkTagReq;
import com.bill.portal.scrm.customer.dto.WxCustomerRemarkReq;

public interface IWxCustomerSyncService {

    /** 流失发起方：客户主动删除成员 */
    int LOST_BY_CUSTOMER = 1;

    /** 流失发起方：员工删除客户 */
    int LOST_BY_EMPLOYEE = 2;

    /** 流失发起方：客户接替失败（原跟进成员已离职，客户悬空） */
    int LOST_BY_TRANSFER_FAIL = 3;

    /** 流失发起方：在职继承自动转接，原跟进关系被系统解除 */
    int LOST_BY_INHERIT_TRANSFER = 4;

    /**
     * 执行全量同步
     * @param tenantId 租户ID
     * @param corpId 企业ID
     * @param syncType 同步类型
     * @param triggerType 触发类型
     */
    void executeFullSync(Long tenantId,String corpId,String syncType, String triggerType);

    /**
     * 同步客户信息
     * @param tenantId 租户ID
     * @param corpId
     * @param syncType 同步类型
     * @param triggerType 触发类型
     * @param externalUserId
     * @param userId
     */
    void syncCustomer(Long tenantId,String corpId, String syncType, String triggerType, String externalUserId, String userId);

    /**
     * 客户流失（好友关系解除：del_external_contact / del_follow_user 事件）。
     * 不做软删除，仅打流失标记（谁主动删的/原因/时间），客户重新添加时自动恢复。
     *
     * @param tenantId 租户ID
     * @param corpId 企业ID
     * @param syncType 同步类型
     * @param triggerType 触发类型
     * @param externalUserId 外部联系人userid
     * @param userId 跟进成员userid
     * @param lostBy 流失发起方：{@link #LOST_BY_CUSTOMER} / {@link #LOST_BY_EMPLOYEE}
     */
    void syncDelCustomer(Long tenantId,String corpId, String syncType, String triggerType, String externalUserId, String userId, int lostBy);

    /**
     * 客户接替失败（transfer_fail 事件，在职/离职继承均可能触发）。
     *
     * <p>按失败原因区分处理：客户拒绝接替（customer_refused）时原跟进
     * 关系保持不变，仅记录日志；其他失败（如接替成员客户数达上限）时
     * 客户已无在职跟进成员，对该客户名下跟进成员已离职/不存在的关系行
     * 打流失标记（lost_by=3），供运营重新分配。
     *
     * @param tenantId 租户ID
     * @param corpId 企业ID
     * @param externalUserId 外部联系人userid
     * @param failReason 企微回调的接替失败原因（customer_refused / customer_limit_exceed）
     */
    void syncTransferFail(Long tenantId, String corpId, String externalUserId, String failReason);

    /**
     * 在职继承自动转接导致的删除（del_external_contact 事件，
     * Source=DELETE_BY_TRANSFER）：客户已被接替成员接替，原成员的
     * 跟进关系被系统解除。仅标记关系行为继承转接解除（lost_by=4），
     * 客户本身已由新成员跟进，不属于真实流失。
     *
     * @param tenantId 租户ID
     * @param corpId 企业ID
     * @param externalUserId 外部联系人userid
     */
    void syncInheritTransferLost(Long tenantId, String corpId, String externalUserId);

    /**
     * 编辑客户企业标签（企微 externalcontact/mark_tag，文档 92697）。
     * 为客户的跟进成员添加/移除企业标签，成功后刷新本地客户快照
     * （extra_data 里的标签随之更新）。
     *
     * @param req corpId / userId（跟进成员）/ externalUserId 必填，
     *            addTag 与 removeTag 不可同时为空
     */
    void markTag(WxCustomerMarkTagReq req);

    /**
     * 修改客户备注信息（企微 externalcontact/remark，文档 92694）。
     * 成功后刷新本地客户快照（extra_data 里的备注随之更新）。
     */
    void remark(WxCustomerRemarkReq req);

}
