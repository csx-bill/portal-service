package com.bill.portal.scrm.callback.handler.impl;

import com.bill.portal.common.core.tenant.TenantBroker;
import com.bill.portal.scrm.callback.dto.SelfWxCallbackPayload;
import com.bill.portal.scrm.callback.dto.SuiteWxCallbackPayload;
import com.bill.portal.scrm.callback.handler.WxEventHandler;
import com.bill.portal.scrm.common.enums.SyncType;
import com.bill.portal.scrm.common.enums.TriggerType;
import com.bill.portal.scrm.contact.service.IWxContactSyncService;
import com.bill.portal.scrm.corp.entity.WxCorpAuth;
import com.bill.portal.scrm.corp.service.IWxCorpAuthService;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.cp.bean.message.WxCpTpXmlMessage;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import org.springframework.stereotype.Service;

import static me.chanjar.weixin.cp.constant.WxCpConsts.ContactChangeType.*;

@Service
@AllArgsConstructor
public class ChangeContactHandler implements WxEventHandler {

    private final IWxCorpAuthService wxCorpAuthService;

    private final IWxContactSyncService wxContactSyncService;


    @Override
    public void handle(Object payload) {
        if (payload == null) {
            return;
        }

        CallbackParam param = buildParam(payload);
        if (param == null || param.tenantId == null) {
            return;
        }

        final String syncType = SyncType.CONTACT_INCREMENT.getCode();
        final String triggerType = TriggerType.CALLBACK.getCode();

        TenantBroker.runAs(param.tenantId, ignored ->
                handleChange(param, syncType, triggerType)
        );
    }


    private void handleChange(CallbackParam p, String syncType, String triggerType) {
        if(CREATE_USER.equals(p.changeType) || UPDATE_USER.equals(p.changeType)){
            wxContactSyncService.syncUser(p.tenantId,p.corpId, syncType, triggerType,p.userId);
        }
        if(DELETE_USER.equals(p.changeType)){
            wxContactSyncService.syncDelUser(p.tenantId,p.corpId, syncType, triggerType,p.userId);
        }

        if(CREATE_PARTY.equals(p.changeType) || UPDATE_PARTY.equals(p.changeType)){
            wxContactSyncService.syncDept(p.tenantId,p.corpId, syncType, triggerType,Long.parseLong(p.deptId));
        }
        if(DELETE_PARTY.equals(p.changeType)){
            wxContactSyncService.syncDelDept(p.tenantId,p.corpId, syncType, triggerType,Long.parseLong(p.deptId));
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
                    msg.getUserID(),
                    msg.getId()
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
                    msg.getUserId(),
                    msg.getId()
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
        private final String userId;
        private final String deptId;
    }

}
