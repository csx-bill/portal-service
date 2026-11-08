package com.bill.portal.scrm.mass.adapters;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bill.portal.common.core.scheduler.TaskExecutor;
import com.bill.portal.common.core.scheduler.support.AbstractMybatisTaskRegistrar;
import com.bill.portal.scrm.mass.entity.WxMassTask;
import com.bill.portal.scrm.mass.job.WxMassTaskJob;
import com.bill.portal.scrm.mass.mapper.WxMassTaskMapper;
import org.springframework.stereotype.Component;


/**
 * 群发任务适配器 —— 将 wx_mass_task 表接入调度引擎
 */
@Component
public class WxMassTaskAdapter extends AbstractMybatisTaskRegistrar<WxMassTask> {

    private final WxMassTaskMapper mapper;
    private final WxMassTaskJob executor;

    public WxMassTaskAdapter(WxMassTaskMapper mapper, WxMassTaskJob executor) {
        this.mapper = mapper;
        this.executor = executor;
    }

    @Override
    public String name() {
        return "wx_mass_task";
    }

    @Override
    public Class<WxMassTask> taskType() {
        return WxMassTask.class;
    }

    @Override
    protected BaseMapper<WxMassTask> mapper() {
        return mapper;
    }

    @Override
    public TaskExecutor<WxMassTask> executor() {
        return executor;
    }
}
