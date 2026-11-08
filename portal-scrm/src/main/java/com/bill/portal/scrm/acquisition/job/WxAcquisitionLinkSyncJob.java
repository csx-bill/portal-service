package com.bill.portal.scrm.acquisition.job;

import com.bill.portal.common.core.datascope.DataScopeBroker;
import com.bill.portal.common.core.tenant.TenantBroker;
import com.bill.portal.scrm.acquisition.service.IWxAcquisitionLinkSyncService;
import com.bill.portal.scrm.common.enums.SyncType;
import com.bill.portal.scrm.common.enums.TriggerType;
import com.bill.portal.scrm.corp.entity.WxCorpAuth;
import com.bill.portal.scrm.corp.service.IWxCorpAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 获客链接全量同步定时任务
 */
@Slf4j
@Component
public class WxAcquisitionLinkSyncJob {

    @Autowired
    private IWxAcquisitionLinkSyncService wxAcquisitionLinkSyncService;

    @Autowired
    private IWxCorpAuthService wxCorpAuthService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void executeFullSync() {
        log.info("--- 定时任务：获客链接全量同步开始 ---");
        try {
            List<WxCorpAuth> authList = wxCorpAuthService.getAuthorizedCorps();

            if (CollectionUtils.isEmpty(authList)) {
                log.info("--- 未找到需要同步的企业，任务结束 ---");
                return;
            }

            for (WxCorpAuth auth : authList) {
                try {
                    TenantBroker.runAs(auth.getTenantId(), (id) ->
                        DataScopeBroker.skip(() ->
                            wxAcquisitionLinkSyncService.executeFullSync(
                                    auth.getTenantId(),
                                    auth.getCorpId(),
                                    SyncType.ACQUISITION_LINK_FULL.getCode(),
                                    TriggerType.CRON.getCode()
                            )));
                } catch (Exception e) {
                    log.error("--- 同步企业[{}]获客链接异常 ---", auth.getCorpId(), e);
                }
            }
        } catch (Exception e) {
            log.error("--- 获客链接定时任务全局异常 ---", e);
        }
    }
}
