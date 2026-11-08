package com.bill.portal.scrm.moment.adapters;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bill.portal.common.core.scheduler.TaskExecutor;
import com.bill.portal.common.core.scheduler.support.AbstractMybatisTaskRegistrar;
import com.bill.portal.scrm.moment.entity.WxMomentTask;
import com.bill.portal.scrm.moment.job.WxMomentTaskJob;
import com.bill.portal.scrm.moment.mapper.WxMomentTaskMapper;
import org.springframework.stereotype.Component;


/**
 * 朋友圈任务适配器 —— 将 wx_moment_task 表接入调度引擎
 */
@Component
public class WxMomentTaskAdapter extends AbstractMybatisTaskRegistrar<WxMomentTask> {

    private final WxMomentTaskMapper mapper;
    private final WxMomentTaskJob executor;

    public WxMomentTaskAdapter(WxMomentTaskMapper mapper, WxMomentTaskJob executor) {
        this.mapper = mapper;
        this.executor = executor;
    }

    @Override
    public String name() {
        return "wx_moment_task";
    }

    @Override
    public Class<WxMomentTask> taskType() {
        return WxMomentTask.class;
    }

    @Override
    protected BaseMapper<WxMomentTask> mapper() {
        return mapper;
    }

    @Override
    public TaskExecutor<WxMomentTask> executor() {
        return executor;
    }
}
