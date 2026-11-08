package com.bill.portal.scrm.mass.job;

import com.bill.portal.common.core.scheduler.TaskExecutor;
import com.bill.portal.common.core.tenant.TenantBroker;
import com.bill.portal.scrm.common.enums.TriggerType;
import com.bill.portal.scrm.mass.entity.WxMassTask;
import com.bill.portal.scrm.mass.service.IWxMassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 企微群发定时任务
 */
@Slf4j
@Component
public class WxMassTaskJob implements TaskExecutor<WxMassTask> {

    @Autowired
    private IWxMassService wxMassService;

    @Override
    public void execute(WxMassTask task) throws Exception {
        String batchNo = UUID.randomUUID().toString().replace("-", "");
        log.info("--- 定时任务：企微群发开始, batchNo={} ---", batchNo);
        TenantBroker.runAs(task.getTenantId(), (id) -> {
            wxMassService.executeTask(task, TriggerType.CRON.getCode(), batchNo);
        });
    }
}
