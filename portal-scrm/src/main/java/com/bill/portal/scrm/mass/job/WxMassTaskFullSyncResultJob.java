package com.bill.portal.scrm.mass.job;

import com.bill.portal.common.core.datascope.DataScopeBroker;
import com.bill.portal.common.core.tenant.TenantBroker;
import com.bill.portal.scrm.corp.entity.WxCorpAuth;
import com.bill.portal.scrm.corp.service.IWxCorpAuthService;
import com.bill.portal.scrm.mass.service.IWxMassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 企微群发任务发送结果同步
 */
@Slf4j
@Component
public class WxMassTaskFullSyncResultJob {

    @Autowired
    private IWxCorpAuthService wxCorpAuthService;

    @Autowired
    private IWxMassService wxMassService;


    @Scheduled(cron = "0 0 2 * * ?")
    public void executeFullSyncMassTaskResult() {
        log.info("--- 定时任务：企微群发结果开始 ---");
        try {
            // 1. 获取需要同步的企业列表
            List<WxCorpAuth> authList = wxCorpAuthService.getAuthorizedCorps();

            if (CollectionUtils.isEmpty(authList)) {
                log.info("--- 未找到需要同步的企业，任务结束 ---");
                return;
            }

            // 2. 遍历执行同步
            for (WxCorpAuth auth : authList) {
                try {
                    TenantBroker.runAs(auth.getTenantId(), (id) ->
                        DataScopeBroker.skip(() ->
                            wxMassService.executeFullSyncTaskResult(auth.getCorpId())));
                } catch (Exception e) {
                    log.error("--- 群发企业[{}]异常 ---", auth.getCorpId(), e);
                }
            }
        } catch (Exception e) {
            log.error("--- 定时任务全局异常 ---", e);
        }
    }
}
