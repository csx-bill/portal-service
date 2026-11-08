package com.bill.portal.scrm.moment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.scrm.moment.dto.WxMomentInvokeLogResp;
import com.bill.portal.scrm.moment.dto.WxMomentTaskBatchStatResp;
import com.bill.portal.scrm.moment.dto.WxMomentTaskStatVO;
import com.bill.portal.scrm.moment.entity.WxMomentInvokeLog;
import com.bill.portal.scrm.moment.entity.WxMomentTask;
import com.bill.portal.scrm.moment.req.WxMomentInvokeLogQueryReq;
import com.bill.portal.scrm.moment.req.WxMomentTaskQueryReq;
import com.bill.portal.scrm.moment.req.WxMomentTaskSaveReq;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.bean.external.msg.Attachment;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface IWxMomentService {

    /**
     * 保存任务
     */
    void saveTask(WxMomentTaskSaveReq req);


    /**
     * 删除任务
     */
    void delTask(Collection<?> idList);

    /**
     * 查询任务
     */
    WxMomentTaskSaveReq getTaskById(Long id);

    /**
     * 分页查询任务列表
     * @param page 分页参数
     * @param req 查询条件
     * @return 分页结果
     */
    IPage<WxMomentTask> taskPage(Page<WxMomentTask> page, WxMomentTaskQueryReq req);


    /**
     * 分页查询调用记录列表
     * @param page 分页参数
     * @param req 查询条件
     * @return 分页结果
     */
    IPage<WxMomentInvokeLog> invokeLogPage(Page<WxMomentInvokeLog> page, WxMomentInvokeLogQueryReq req);

    /**
     * 查询调用记录详情（含内容快照）
     */
    WxMomentInvokeLogResp getInvokeById(Long id);


    /**
     * 执行任务
     * @param task 任务
     * @param triggerType 触发类型
     * @param batchNo 批次号
     */
    void executeTask(WxMomentTask task, String triggerType, String batchNo) throws IOException, WxErrorException;

    /**
     * 执行任务并记录日志
     * @param task 任务实体
     * @param triggerType 触发类型
     * @param batchNo 批次号
     */
    void executeTaskWithLog(WxMomentTask task, String triggerType, List<Attachment> attachments, String batchNo);

    /**
     * 全量同步任务的发送结果（定时任务用，近3天窗口）
     * @param corpId
     */
    void executeFullSyncTaskResult(String corpId) throws WxErrorException;

    /**
     * 同步指定时间窗内未完成任务的创建状态与逐成员发表结果
     * @param corpId 企业ID
     * @param days 时间窗口（天）
     * @return 处理的批次（调用记录）数量
     */
    int executeSyncTaskResult(String corpId, int days) throws WxErrorException;
    /**
     * 同步任务的发送结果
     */
    void syncTaskResult(WxMomentInvokeLog invokeLog) throws WxErrorException;

    /**
     * 任务维度发表统计（全部批次合计）
     * @param taskIds 任务ID集合
     * @return 各任务的发表统计（无发表记录的任务不在返回中）
     */
    List<WxMomentTaskStatVO> statByTaskIds(Collection<Long> taskIds);

    /**
     * 单任务发表统计：全部批次合计 + 各批次明细（含成员维度）
     * @param taskId 任务ID
     */
    WxMomentTaskBatchStatResp statTaskBatches(Long taskId);
}
