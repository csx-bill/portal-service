package com.bill.portal.scrm.callback.handler.impl;

import com.bill.portal.common.core.tenant.TenantBroker;
import com.bill.portal.scrm.callback.dto.SelfWxCallbackPayload;
import com.bill.portal.scrm.callback.dto.SuiteWxCallbackPayload;
import com.bill.portal.scrm.callback.handler.WxEventHandler;
import com.bill.portal.scrm.common.enums.SyncType;
import com.bill.portal.scrm.common.enums.TriggerType;
import com.bill.portal.scrm.corp.entity.WxCorpAuth;
import com.bill.portal.scrm.corp.service.IWxCorpAuthService;
import com.bill.portal.scrm.groupchat.service.IWxGroupChatSyncService;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.cp.bean.message.WxCpTpXmlMessage;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import org.springframework.stereotype.Service;

import static me.chanjar.weixin.cp.constant.WxCpConsts.ExternalChatChangeType.DISMISS;


@Service
@AllArgsConstructor
public class ChangeExternalChatHandler implements WxEventHandler {

    private final IWxCorpAuthService wxCorpAuthService;

    private final IWxGroupChatSyncService wxGroupChatSyncService;


    @Override
    public void handle(Object payload) {
        if (payload == null) {
            return;
        }

        CallbackParam param = buildParam(payload);
        if (param == null || param.tenantId == null) {
            return;
        }

        final String syncType = SyncType.GROUP_CHAT_INCREMENT.getCode();
        final String triggerType = TriggerType.CALLBACK.getCode();

        TenantBroker.runAs(param.tenantId, ignored ->
                handleChange(param, syncType, triggerType)
        );
    }


    private void handleChange(CallbackParam p, String syncType, String triggerType) {
        if(!DISMISS.equals(p.changeType)){
            wxGroupChatSyncService.syncGroupChat(p.tenantId,p.corpId, syncType, triggerType,p.chatId,
                    p.updateDetail, p.lastMemVer, p.curMemVer);
        }else {
            wxGroupChatSyncService.syncDismissGroupChat(p.tenantId,p.corpId, syncType, triggerType,p.chatId);
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
                    msg.getChatId(),
                    msg.getUpdateDetail(),
                    null,
                    null
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
                    msg.getChatId(),
                    msg.getUpdateDetail(),
                    msg.getLastMemVer() == null ? null : Long.valueOf(msg.getLastMemVer()),
                    msg.getCurMemVer() == null ? null : Long.valueOf(msg.getCurMemVer())
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
        private final String chatId;
        /** update 事件的变更详情类型（其他事件为 null） */
        private final String updateDetail;
        /** update 成员变更事件的前/后群成员版本号（自建应用回调携带，服务商为 null） */
        private final Long lastMemVer;
        private final Long curMemVer;
    }
}
