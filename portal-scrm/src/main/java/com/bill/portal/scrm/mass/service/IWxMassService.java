package com.bill.portal.scrm.mass.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.scrm.mass.dto.WxMassInvokeLogResp;
import com.bill.portal.scrm.mass.dto.WxMassTaskBatchStatResp;
import com.bill.portal.scrm.mass.dto.WxMassTaskStatVO;
import com.bill.portal.scrm.mass.entity.WxMassInvokeLog;
import com.bill.portal.scrm.mass.entity.WxMassTask;
import com.bill.portal.scrm.mass.req.WxMassInvokeLogQuery;
import com.bill.portal.scrm.mass.req.WxMassTaskQuery;
import com.bill.portal.scrm.mass.req.WxMassTaskSaveReq;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.bean.external.msg.Attachment;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface IWxMassService {

    /**
     * 保存群发任务
     */
    void saveTask(WxMassTaskSaveReq req);


    /**
     * 删除群发任务
     */
    void delTask(Collection<?> idList);

    /**
     * 查询群发任务
     */
    WxMassTaskSaveReq getTaskById(Long id);

    /**
     * 分页查询任务列表
     * @param page 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<WxMassTask> taskPage(Page<WxMassTask> page, WxMassTaskQuery query);


    /**
     * 分页查询调用记录列表
     * @param page 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<WxMassInvokeLog> invokeLogPage(Page<WxMassInvokeLog> page, WxMassInvokeLogQuery query);

    /**
     * 查询调用记录详情（含发送目标与内容快照）
     */
    WxMassInvokeLogResp getInvokeById(Long id);


    /**
     * 执行任务
     * @param task 任务
     * @param triggerType 触发类型
     * @param batchNo 批次号
     */
    void executeTask(WxMassTask task, String triggerType, String batchNo) throws IOException, WxErrorException;

    void executeTaskWithLog(WxMassTask task, String triggerType, String sender, List<Attachment> attachments, String batchNo);

    /**
     * 全量同步群发任务的发送结果（定时任务用，近3天窗口）
     * @param corpId
     */
    void executeFullSyncTaskResult(String corpId) throws WxErrorException;

    /**
     * 同步指定时间窗内已发送任务的送达结果
     * @param corpId 企业ID
     * @param days 时间窗口（天）
     * @return 处理的批次（调用记录）数量
     */
    int executeSyncTaskResult(String corpId, int days) throws WxErrorException;

    /**
     * 同步群发任务的发送结果
     */
    void syncTaskResult(WxMassInvokeLog invokeLog,String userId) throws WxErrorException;

    /**
     * 任务维度送达统计（全部批次合计）
     * @param taskIds 任务ID集合
     * @return 各任务的送达统计（无发送记录的任务不在返回中）
     */
    List<WxMassTaskStatVO> statByTaskIds(Collection<Long> taskIds);

    /**
     * 单任务送达统计：全部批次合计 + 各批次明细（含成员维度）
     * @param taskId 任务ID
     */
    WxMassTaskBatchStatResp statTaskBatches(Long taskId);

}
