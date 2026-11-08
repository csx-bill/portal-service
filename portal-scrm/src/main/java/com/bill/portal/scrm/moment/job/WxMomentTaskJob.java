package com.bill.portal.scrm.moment.job;

import com.bill.portal.common.core.scheduler.TaskExecutor;
import com.bill.portal.common.core.tenant.TenantBroker;
import com.bill.portal.scrm.common.enums.TriggerType;
import com.bill.portal.scrm.moment.entity.WxMomentTask;
import com.bill.portal.scrm.moment.service.IWxMomentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 企微朋友圈定时任务
 */
@Slf4j
@Component
public class WxMomentTaskJob implements TaskExecutor<WxMomentTask> {

    @Autowired
    private IWxMomentService wxMomentService;

    @Override
    public void execute(WxMomentTask task) throws Exception {
        String batchNo = UUID.randomUUID().toString().replace("-", "");
        log.info("--- 定时任务：企微朋友圈开始, batchNo={} ---", batchNo);
        TenantBroker.runAs(task.getTenantId(), (id) -> {
            wxMomentService.executeTask(task, TriggerType.CRON.getCode(), batchNo);
        });
    }
}
