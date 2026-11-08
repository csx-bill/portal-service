package com.bill.portal.scrm.callback.handler.impl;

import com.bill.portal.common.core.tenant.TenantBroker;
import com.bill.portal.scrm.callback.dto.SelfWxCallbackPayload;
import com.bill.portal.scrm.callback.dto.SuiteWxCallbackPayload;
import com.bill.portal.scrm.callback.handler.WxEventHandler;
import com.bill.portal.scrm.common.enums.SyncType;
import com.bill.portal.scrm.common.enums.TriggerType;
import com.bill.portal.scrm.corp.entity.WxCorpAuth;
import com.bill.portal.scrm.corp.service.IWxCorpAuthService;
import com.bill.portal.scrm.tag.service.IWxCorpTagSyncService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.bean.message.WxCpTpXmlMessage;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import org.springframework.stereotype.Service;

import static me.chanjar.weixin.cp.constant.WxCpConsts.ExternalTagChangeType.CREATE;
import static me.chanjar.weixin.cp.constant.WxCpConsts.ExternalTagChangeType.DELETE;
import static me.chanjar.weixin.cp.constant.WxCpConsts.ExternalTagChangeType.SHUFFLE;
import static me.chanjar.weixin.cp.constant.WxCpConsts.ExternalTagChangeType.UPDATE;

/**
 * 企业客户标签事件处理器（create/update/delete/shuffle）。
 *
 * <p>标签库数据量小且管理端操作低频，四种事件统一触发标签全量同步：
 * create/update 重拉最新标签数据；delete 由企微标签库返回的 deleted
 * 标记落 del_flag；shuffle 重拉最新 order。
 */
@Slf4j
@Service
@AllArgsConstructor
public class ChangeExternalTagHandler implements WxEventHandler {

    private final IWxCorpAuthService wxCorpAuthService;

    private final IWxCorpTagSyncService wxCorpTagSyncService;

    @Override
    public void handle(Object payload) {
        if (payload == null) {
            return;
        }

        CallbackParam param = buildParam(payload);
        if (param == null || param.tenantId == null) {
            return;
        }

        TenantBroker.runAs(param.tenantId, ignored ->
                handleChange(param)
        );
    }

    private void handleChange(CallbackParam p) {
        // create/update/delete/shuffle 统一触发标签全量同步（标签库小、管理端操作低频）
        log.info("[tag-event] 标签变更事件触发全量同步：corpId={}, changeType={}, id={}, tagType={}",
                p.corpId, p.changeType, p.id, p.tagType);
        wxCorpTagSyncService.executeFullSync(
                p.tenantId,
                p.corpId,
                SyncType.TAG_FULL.getCode(),
                TriggerType.CALLBACK.getCode()
        );
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
                    msg.getId(),
                    msg.getTagType()
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
                    msg.getId(),
                    msg.getTagType()
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
        /** create/update/delete/shuffle */
        private final String changeType;
        /** 标签/标签组ID */
        private final String id;
        /** tag=标签 / tag_group=标签组（shuffle 事件为 null） */
        private final String tagType;
    }
}
