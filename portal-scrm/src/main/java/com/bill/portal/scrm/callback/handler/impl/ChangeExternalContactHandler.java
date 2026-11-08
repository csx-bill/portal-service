package com.bill.portal.scrm.callback.handler.impl;

import com.bill.portal.common.core.tenant.TenantBroker;
import com.bill.portal.scrm.callback.dto.SelfWxCallbackPayload;
import com.bill.portal.scrm.callback.dto.SuiteWxCallbackPayload;
import com.bill.portal.scrm.callback.handler.WxEventHandler;
import com.bill.portal.scrm.common.enums.SyncType;
import com.bill.portal.scrm.common.enums.TriggerType;
import com.bill.portal.scrm.corp.entity.WxCorpAuth;
import com.bill.portal.scrm.corp.service.IWxCorpAuthService;
import com.bill.portal.scrm.customer.service.IWxCustomerSyncService;
import com.bill.portal.scrm.welcome.service.IWxWelcomeMsgService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.bean.message.WxCpTpXmlMessage;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import org.springframework.stereotype.Service;

import static me.chanjar.weixin.cp.constant.WxCpConsts.ExternalContactChangeType.*;

@Slf4j
@Service
@AllArgsConstructor
public class ChangeExternalContactHandler implements WxEventHandler {
    private final IWxCorpAuthService wxCorpAuthService;

    private final IWxCustomerSyncService wxCustomerSyncService;

    private final IWxWelcomeMsgService wxWelcomeMsgService;

    @Override
    public void handle(Object payload) {
        if (payload == null) {
            return;
        }

        CallbackParam param = buildParam(payload);
        if (param == null || param.tenantId == null) {
            return;
        }

        final String syncType = SyncType.CUSTOMER_INCREMENT.getCode();
        final String triggerType = TriggerType.CALLBACK.getCode();

        TenantBroker.runAs(param.tenantId, ignored ->
                handleChange(param, syncType, triggerType)
        );
    }

    private void handleChange(CallbackParam p, String syncType, String triggerType) {
        if (ADD_EXTERNAL_CONTACT.equals(p.changeType)
                || ADD_HALF_EXTERNAL_CONTACT.equals(p.changeType)
                || EDIT_EXTERNAL_CONTACT.equals(p.changeType)) {

            // 正常添加与免验证添加均携带欢迎语码：先发欢迎语（约20秒有效），再同步客户
            if (ADD_EXTERNAL_CONTACT.equals(p.changeType)
                    || ADD_HALF_EXTERNAL_CONTACT.equals(p.changeType)) {
                wxWelcomeMsgService.sendForAddContact(p.corpId, p.userId, p.welcomeCode);
            }

            wxCustomerSyncService.syncCustomer(
                    p.tenantId,
                    p.corpId,
                    syncType,
                    triggerType,
                    p.externalUserId,
                    p.userId
            );
        }

        if (DEL_EXTERNAL_CONTACT.equals(p.changeType)) {
            if ("DELETE_BY_TRANSFER".equals(p.source)) {
                // 在职继承自动转接：客户已被新成员接替，原关系为系统解除而非流失
                wxCustomerSyncService.syncInheritTransferLost(
                        p.tenantId,
                        p.corpId,
                        p.externalUserId
                );
            } else {
                // 客户删除成员：客户主动流失
                wxCustomerSyncService.syncDelCustomer(
                        p.tenantId,
                        p.corpId,
                        syncType,
                        triggerType,
                        p.externalUserId,
                        p.userId,
                        IWxCustomerSyncService.LOST_BY_CUSTOMER
                );
            }
        }

        if (DEL_FOLLOW_USER.equals(p.changeType)) {
            // 员工删除客户（含成员离职/禁用场景）
            wxCustomerSyncService.syncDelCustomer(
                    p.tenantId,
                    p.corpId,
                    syncType,
                    triggerType,
                    p.externalUserId,
                    p.userId,
                    IWxCustomerSyncService.LOST_BY_EMPLOYEE
            );
        }

        if (TRANSFER_FAIL.equals(p.changeType)) {
            // 客户接替失败：按失败原因处理（拒绝不打流失，其他标记客户悬空）
            wxCustomerSyncService.syncTransferFail(
                    p.tenantId,
                    p.corpId,
                    p.externalUserId,
                    p.failReason
            );
        }
    }


    private CallbackParam buildParam(Object payload) {
        if (payload instanceof SuiteWxCallbackPayload) {
            WxCpTpXmlMessage msg = ((SuiteWxCallbackPayload) payload).getXmlMessage();
            WxCorpAuth auth = wxCorpAuthService.getCorpAuth(msg.getToUserName());
            if (auth == null) {
                return null;
            }
            return new CallbackParam(
                    auth.getTenantId(),
                    msg.getToUserName(),
                    msg.getChangeType(),
                    msg.getExternalUserID(),
                    msg.getUserID(),
                    msg.getWelcomeCode(),
                    msg.getFailReason(),
                    msg.getSource()
            );
        }

        if (payload instanceof SelfWxCallbackPayload) {
            WxCpXmlMessage msg = ((SelfWxCallbackPayload) payload).getXmlMessage();
            WxCorpAuth auth = wxCorpAuthService.getCorpAuth(msg.getToUserName());
            if (auth == null) {
                return null;
            }
            return new CallbackParam(
                    auth.getTenantId(),
                    msg.getToUserName(),
                    msg.getChangeType(),
                    msg.getExternalUserId(),
                    msg.getUserId(),
                    msg.getWelcomeCode(),
                    msg.getFailReason(),
                    msg.getSource()
            );
        }

        return null;
    }


    /**
     * 仅用于承载参数，无任何行为
     */
    @AllArgsConstructor
    private static class CallbackParam {
        private final Long tenantId;
        private final String corpId;
        private final String changeType;
        private final String externalUserId;
        private final String userId;
        /** 添加类事件携带的欢迎语码（其他事件为 null） */
        private final String welcomeCode;
        /** 接替失败事件携带的失败原因（其他事件为 null） */
        private final String failReason;
        /** 删除事件携带的操作来源（DELETE_BY_TRANSFER=在职继承自动转接，其他事件为 null） */
        private final String source;
    }
}
