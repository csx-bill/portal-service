package com.bill.portal.scrm.moment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.common.core.exception.CheckedException;
import com.bill.portal.common.core.scheduler.ScheduleType;
import com.bill.portal.common.core.scheduler.SchedulerEngine;
import com.bill.portal.scrm.service.IWxMediaService;
import com.bill.portal.scrm.common.constant.WxMomentConstants;
import com.bill.portal.scrm.corp.strategy.WxCorpApiStrategy;
import com.bill.portal.scrm.moment.dto.WxMomentInvokeLogResp;
import com.bill.portal.scrm.moment.dto.WxMomentBatchStatVO;
import com.bill.portal.scrm.moment.dto.WxMomentTaskBatchStatResp;
import com.bill.portal.scrm.moment.dto.WxMomentTaskStatVO;
import com.bill.portal.scrm.moment.dto.WxMomentUserStatVO;
import com.bill.portal.scrm.moment.entity.WxMomentContent;
import com.bill.portal.scrm.moment.entity.WxMomentContentLog;
import com.bill.portal.scrm.moment.entity.WxMomentInvokeLog;
import com.bill.portal.scrm.moment.entity.WxMomentSendLog;
import com.bill.portal.scrm.moment.entity.WxMomentTask;
import com.bill.portal.scrm.moment.mapper.WxMomentContentLogMapper;
import com.bill.portal.scrm.moment.mapper.WxMomentContentMapper;
import com.bill.portal.scrm.moment.mapper.WxMomentInvokeLogMapper;
import com.bill.portal.scrm.moment.mapper.WxMomentSendLogMapper;
import com.bill.portal.scrm.moment.mapper.WxMomentTaskMapper;
import com.bill.portal.scrm.moment.req.WxMomentContentSaveReq;
import com.bill.portal.scrm.moment.req.WxMomentInvokeLogQueryReq;
import com.bill.portal.scrm.moment.req.WxMomentTaskQueryReq;
import com.bill.portal.scrm.moment.req.WxMomentTaskSaveReq;
import com.bill.portal.scrm.moment.service.IWxMomentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.WxCpExternalContactService;
import me.chanjar.weixin.cp.bean.external.WxCpAddMomentResult;
import me.chanjar.weixin.cp.bean.external.WxCpAddMomentTask;
import me.chanjar.weixin.cp.bean.external.WxCpGetMomentTask;
import me.chanjar.weixin.cp.bean.external.WxCpGetMomentTaskResult;
import me.chanjar.weixin.cp.bean.external.moment.ExternalContactList;
import me.chanjar.weixin.cp.bean.external.moment.SenderList;
import me.chanjar.weixin.cp.bean.external.moment.VisibleRange;
import me.chanjar.weixin.cp.bean.external.msg.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 企微朋友圈核心服务
 */
@Slf4j
@Service
@AllArgsConstructor
public class WxMomentServiceImpl implements IWxMomentService {

    private final WxCorpApiStrategy wxCorpApiStrategy;

    private final WxMomentTaskMapper wxMomentTaskMapper;

    private final WxMomentContentMapper wxMomentContentMapper;

    private final WxMomentInvokeLogMapper wxMomentInvokeLogMapper;

    private final WxMomentContentLogMapper wxMomentContentLogMapper;

    private final WxMomentSendLogMapper wxMomentSendLogMapper;

    private final IWxMediaService wxMediaService;

    private final SchedulerEngine engine;

    @Override
    public void saveTask(WxMomentTaskSaveReq req) {
        // 参数校验保持不变
        String visibleType = req.getVisibleType();
        if(visibleType.equals(WxMomentConstants.USER)){
            List<String> visibleUserList = req.getVisibleUserList();
            if(CollectionUtils.isEmpty(visibleUserList)){
                throw new CheckedException("成员不能为空");
            }
        }

        if(visibleType.equals(WxMomentConstants.DEPARTMENT)){
            List<String> visibleDeptList = req.getVisibleDeptList();
            if(CollectionUtils.isEmpty(visibleDeptList)){
                throw new CheckedException("部门不能为空");
            }
        }

        if(visibleType.equals(WxMomentConstants.TAG)){
            List<String> visibleTagList = req.getVisibleTagList();
            if(CollectionUtils.isEmpty(visibleTagList)){
                throw new CheckedException("标签不能为空");
            }
        }

        if(CollectionUtils.isEmpty(req.getContentList())){
            throw new CheckedException("内容不能为空");
        }

        // 转换任务实体
        WxMomentTask wxMomentTask = BeanUtil.copyProperties(req, WxMomentTask.class);

        // 处理立即执行的时间
        if (ScheduleType.IMMEDIATE.equals(req.getExecuteType())) {
            // 如果是立即执行，将 scheduled_time 设置为当前时间
            wxMomentTask.setScheduledTime(LocalDateTime.now());
        }

        // 保存或更新任务
        if (req.getId() != null) {
            // 更新任务
            wxMomentTaskMapper.updateById(wxMomentTask);
            // 删除原有内容
            wxMomentContentMapper.delete(Wrappers.<WxMomentContent>lambdaQuery().eq(WxMomentContent::getTaskId, req.getId()));
        } else {
            // 新增任务
            wxMomentTaskMapper.insert(wxMomentTask);
        }

        // 保存内容列表
        List<WxMomentContent> wxMassContentList = req.getContentList().stream()
                .map(content -> {
                    WxMomentContent wxMomentContent = BeanUtil.copyProperties(content, WxMomentContent.class);
                    wxMomentContent.setTaskId(wxMomentTask.getId());
                    return wxMomentContent;
                })
                .collect(Collectors.toList());
        wxMomentContentMapper.insert(wxMassContentList);

        if (wxMomentTask.getScheduleType().isRecurring()) {
            engine.schedule(wxMomentTask.getId());
        }

    }

    @Override
    public void delTask(Collection<?> idList) {
        for (Object id : idList) {
            engine.cancel((Long) id);
        }
        wxMomentTaskMapper.deleteByIds(idList);
    }

    @Override
    public WxMomentTaskSaveReq getTaskById(Long id) {
        WxMomentTask wxMomentTask = wxMomentTaskMapper.selectById(id);
        WxMomentTaskSaveReq req = BeanUtil.copyProperties(wxMomentTask, WxMomentTaskSaveReq.class);

        // 查询任务内容列表并转换为WxMomentContentSaveReq
        List<WxMomentContent> contents = wxMomentContentMapper.selectList(
                Wrappers.<WxMomentContent>lambdaQuery().eq(WxMomentContent::getTaskId, id)
        );
        req.setContentList(contents.stream()
                .map(content -> {
                    return BeanUtil.copyProperties(content, WxMomentContentSaveReq.class);
                })
                .collect(Collectors.toList())
        );
        return req;
    }

    @Override
    public IPage<WxMomentTask> taskPage(Page<WxMomentTask> page, WxMomentTaskQueryReq req) {
        // 构建查询条件
        LambdaQueryWrapper<WxMomentTask> queryWrapper = Wrappers.lambdaQuery();

        if (req.getCorpId() != null) {
            queryWrapper.eq(WxMomentTask::getCorpId, req.getCorpId());
        }
        if (req.getVisibleType() != null) {
            queryWrapper.eq(WxMomentTask::getVisibleType, req.getVisibleType());
        }
        if (req.getStatus() != null) {
            queryWrapper.eq(WxMomentTask::getStatus, req.getStatus());
        }
        if (StringUtils.hasText(req.getTaskName())) {
            queryWrapper.like(WxMomentTask::getTaskName, req.getTaskName());
        }
        // 按可见成员过滤（仅 visible_type=USER 的任务携带成员清单）
        if (StringUtils.hasText(req.getSender())) {
            queryWrapper.eq(WxMomentTask::getVisibleType, "user")
                    .apply("JSON_CONTAINS(visible_user_list, JSON_QUOTE({0}))", req.getSender());
        }
        // 按可见部门过滤（仅 visible_type=DEPARTMENT 的任务携带部门清单）
        if (req.getDeptId() != null) {
            queryWrapper.eq(WxMomentTask::getVisibleType, "department")
                    .apply("JSON_CONTAINS(visible_dept_list, JSON_QUOTE(CAST({0} AS CHAR)))", req.getDeptId());
        }

        // 添加排序条件
        queryWrapper.orderByDesc(WxMomentTask::getCreateTime);

        return wxMomentTaskMapper.selectPage(page,queryWrapper);
    }

    @Override
    public IPage<WxMomentInvokeLog> invokeLogPage(Page<WxMomentInvokeLog> page, WxMomentInvokeLogQueryReq req) {
        // 构建查询条件
        LambdaQueryWrapper<WxMomentInvokeLog> queryWrapper = Wrappers.lambdaQuery();

        if (req.getCorpId() != null) {
            queryWrapper.eq(WxMomentInvokeLog::getCorpId, req.getCorpId());
        }

        if (req.getTaskId() != null) {
            queryWrapper.eq(WxMomentInvokeLog::getTaskId, req.getTaskId());
        }

        if (req.getVisibleType() != null) {
            queryWrapper.eq(WxMomentInvokeLog::getVisibleType, req.getVisibleType());
        }
        if (req.getStatus() != null) {
            queryWrapper.eq(WxMomentInvokeLog::getStatus, req.getStatus());
        }
        if (StringUtils.hasText(req.getTaskName())) {
            queryWrapper.like(WxMomentInvokeLog::getTaskName, req.getTaskName());
        }

        // 添加排序条件
        queryWrapper.orderByDesc(WxMomentInvokeLog::getCreateTime);

        return wxMomentInvokeLogMapper.selectPage(page,queryWrapper);
    }

    @Override
    public WxMomentInvokeLogResp getInvokeById(Long id) {
        WxMomentInvokeLog wxMomentInvokeLog = wxMomentInvokeLogMapper.selectById(id);
        if (wxMomentInvokeLog == null) {
            throw new CheckedException("调用记录不存在");
        }
        WxMomentInvokeLogResp resp = BeanUtil.copyProperties(wxMomentInvokeLog, WxMomentInvokeLogResp.class);

        // 查询任务内容列表并转换为WxMomentContentSaveReq
        List<WxMomentContentLog> contents = wxMomentContentLogMapper.selectList(
                Wrappers.<WxMomentContentLog>lambdaQuery().eq(WxMomentContentLog::getInvokeId, id)
        );
        resp.setContentList(contents.stream()
                .map(content -> {
                    return BeanUtil.copyProperties(content, WxMomentContentSaveReq.class);
                })
                .collect(Collectors.toList())
        );
        return resp;
    }

    @Override
    public void executeTask(WxMomentTask task, String triggerType, String batchNo) throws IOException, WxErrorException {
        // 预先构建并上传附件资源
        List<Attachment> attachments = buildAndUploadAttachments(task);
        executeTaskWithLog(task, triggerType, attachments, batchNo);

    }

    /**
     * 执行任务并记录日志
     * @param task 任务实体
     * @param triggerType 触发类型
     */
    @Override
    public void executeTaskWithLog(WxMomentTask task,String triggerType,List<Attachment> attachments, String batchNo){
        WxMomentInvokeLog invokeLog = BeanUtil.copyProperties(task, WxMomentInvokeLog.class);
        invokeLog.setId(null);
        invokeLog.setTaskId(task.getId());
        invokeLog.setTriggerType(triggerType);
        invokeLog.setStatus("SUCCESS");
        invokeLog.setBatchNo(batchNo);
        try {
            WxCpExternalContactService externalContactService = wxCorpApiStrategy.getWxCpService(task.getCorpId()).getExternalContactService();

            WxCpAddMomentTask wxCpAddMomentTask = new WxCpAddMomentTask();

            VisibleRange visibleRange = new VisibleRange();
            SenderList senderList = new SenderList();
            // 根据群发类型设置不同的目标参数
            switch (task.getVisibleType()) {
                case WxMomentConstants.DEPARTMENT:
                    senderList.setDepartmentList(task.getVisibleDeptList());
                    break;
                case WxMomentConstants.USER:
                    senderList.setUserList(task.getVisibleUserList());
                    break;
                case WxMomentConstants.TAG:
                    ExternalContactList externalContactList = new ExternalContactList();
                    externalContactList.setTagList(task.getVisibleTagList());
                    visibleRange.setExternalContactList(externalContactList);
                    break;
                default:
                    break;
            }
            visibleRange.setSenderList(senderList);
            wxCpAddMomentTask.setVisibleRange(visibleRange);

            // 处理文本消息（文本无需上传，每次直接设置即可）
            List<WxMomentContent> wxMomentContents = wxMomentContentMapper.selectList(Wrappers.<WxMomentContent>lambdaQuery()
                    .eq(WxMomentContent::getTaskId, task.getId())
            );
            for (WxMomentContent wxMassContent : wxMomentContents) {
                if ("text".equals(wxMassContent.getMsgType())) {
                    Text text = new Text();
                    text.setContent(wxMassContent.getTextContent());
                    wxCpAddMomentTask.setText(text);
                    break;
                }
            }

            // 设置附件列表
            wxCpAddMomentTask.setAttachments(attachments);
            // 调用企微API发送群发消息
            WxCpAddMomentResult wxCpAddMomentResult = externalContactService.addMomentTask(wxCpAddMomentTask);
            invokeLog.setJobid(wxCpAddMomentResult.getJobId());
            invokeLog.setExtraData(wxCpAddMomentResult.toJson());

            // 1. 保存调用日志
            wxMomentInvokeLogMapper.insert(invokeLog);
            // 2. 提取循环外的不变参数，避免在循环中重复调用 getter
            final Long taskId = task.getId();
            final Long invokeId = invokeLog.getId();

            // 3. 使用 Stream 进行转换和批量赋值
            List<WxMomentContentLog> wxMomentContentLogs = wxMomentContents.stream()
                    .map(content -> {
                        WxMomentContentLog log = BeanUtil.copyProperties(content, WxMomentContentLog.class);
                        log.setId(null);
                        log.setTaskId(taskId);
                        log.setInvokeId(invokeId);
                        return log;
                    })
                    // 提前指定 ArrayList 大小，避免扩容消耗
                    .collect(Collectors.toCollection(() -> new ArrayList<>(wxMomentContents.size())));

            // 4. 批量保存
            wxMomentContentLogMapper.insert(wxMomentContentLogs);

        }catch (Exception e){
            invokeLog.setStatus("FAIL");
            invokeLog.setErrmsg(e.getMessage());
            wxMomentInvokeLogMapper.insert(invokeLog);
        }

    }

    /**
     * 全量同步任务的发送结果
     * @param corpId
     */
    @Override
    public void executeFullSyncTaskResult(String corpId) throws WxErrorException {
        this.executeSyncTaskResult(corpId, 3);
    }

    /**
     * 同步指定时间窗内未完成任务的创建状态与逐成员发表结果
     * @param corpId 企业ID
     * @param days 时间窗口（天，只处理窗口内创建的调用）
     */
    @Override
    public int executeSyncTaskResult(String corpId, int days) throws WxErrorException {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<WxMomentInvokeLog> invokeLogs = wxMomentInvokeLogMapper.selectList(
                Wrappers.<WxMomentInvokeLog>lambdaQuery()
                        .eq(WxMomentInvokeLog::getCorpId,corpId)
                        .eq(WxMomentInvokeLog::getStatus, "SUCCESS")
                        .ge(WxMomentInvokeLog::getCreateTime, since)
                        .isNotNull(WxMomentInvokeLog::getJobid)
                        .and(wrapper -> wrapper
                                .isNull(WxMomentInvokeLog::getMomentStatus)
                                .or()
                                .ne(WxMomentInvokeLog::getMomentStatus, 3)
                        )
        );
        for (WxMomentInvokeLog invokeLog : invokeLogs) {
            this.syncTaskResult(invokeLog);
        }
        return invokeLogs.size();

    }

    /**
     * 同步任务的发送结果
     */
    @Override
    public void syncTaskResult(WxMomentInvokeLog invokeLog) throws WxErrorException {
        WxCpExternalContactService externalContactService = wxCorpApiStrategy.getWxCpService(invokeLog.getCorpId()).getExternalContactService();
        WxCpGetMomentTaskResult momentTaskResult = externalContactService.getMomentTaskResult(invokeLog.getJobid());
        Integer status = momentTaskResult.getStatus();
        invokeLog.setMomentStatus(status);
        if(status==3){
            invokeLog.setMomentId(momentTaskResult.getResult().getMomentId());
            // 创建完成后拉取逐成员发表结果并落库（统计报表数据源）
            syncSendLogs(invokeLog);
        }
        wxMomentInvokeLogMapper.updateById(invokeLog);
    }

    /**
     * 拉取朋友圈逐成员发表结果（get_moment_task）并落库。
     * publishStatus 企微返回 "1"-已发表 / "2"-未发表（WxJava 映射为 String）
     */
    private void syncSendLogs(WxMomentInvokeLog invokeLog) throws WxErrorException {
        if (invokeLog.getMomentId() == null) {
            return;
        }
        WxCpExternalContactService externalContactService = wxCorpApiStrategy.getWxCpService(invokeLog.getCorpId()).getExternalContactService();
        String cursor = "";
        List<WxMomentSendLog> logs = new ArrayList<>();
        do {
            WxCpGetMomentTask taskResult = externalContactService.getMomentTask(invokeLog.getMomentId(), cursor, 500);
            for (WxCpGetMomentTask.MomentTaskItem item : taskResult.getTaskList()) {
                WxMomentSendLog sendLog = new WxMomentSendLog();
                sendLog.setCorpId(invokeLog.getCorpId());
                sendLog.setTaskId(invokeLog.getTaskId());
                sendLog.setInvokeId(invokeLog.getId());
                sendLog.setUserId(item.getUserId());
                sendLog.setPublishStatus("1".equals(item.getPublishStatus()) ? 1 : 0);
                sendLog.setTenantId(invokeLog.getTenantId());
                sendLog.setCreateTime(LocalDateTime.now());
                sendLog.setUpdateTime(LocalDateTime.now());
                logs.add(sendLog);
            }
            cursor = taskResult.getNextCursor();
        } while (cursor != null && !cursor.isEmpty());
        if (!logs.isEmpty()) {
            wxMomentSendLogMapper.batchUpsert(logs);
        }
    }


    /**
     * 预先构建并上传附件资源，避免多发送者重复上传
     * @param task 任务实体
     * @return 附件列表
     */
    private List<Attachment> buildAndUploadAttachments(WxMomentTask task) throws IOException, WxErrorException {
        List<WxMomentContent> wxMomentContents = wxMomentContentMapper.selectList(
                Wrappers.<WxMomentContent>lambdaQuery().eq(WxMomentContent::getTaskId, task.getId())
        );
        Integer attachmentType = 1;
        List<Attachment> attachments = new ArrayList<>();
        for (WxMomentContent wxMassContent : wxMomentContents) {
            Attachment attachment = new Attachment();
            String msgType = wxMassContent.getMsgType();
            String url = wxMassContent.getUrl();

            switch (msgType) {
                case "image":
                    Image image = new Image();
                    String imageMediaId = wxMediaService.uploadAttachment(task.getCorpId(), msgType,attachmentType, wxMassContent.getFilename(), url);
                    image.setMediaId(imageMediaId);
                    attachment.setImage(image);
                    attachments.add(attachment);
                    break;
                case "link":
                    Link link = new Link();
                    link.setPicUrl(url);
                    link.setUrl(wxMassContent.getLinkUrl());
                    link.setTitle(wxMassContent.getLinkTitle());
                    link.setDesc(wxMassContent.getLinkDesc());
                    attachment.setLink(link);
                    attachments.add(attachment);
                    break;
                case "video":
                    Video video = new Video();
                    String videoMediaId = wxMediaService.uploadAttachment(task.getCorpId(), msgType,attachmentType, wxMassContent.getFilename(), url);
                    video.setMediaId(videoMediaId);
                    attachment.setVideo(video);
                    attachments.add(attachment);
                    break;
                default:
                    log.warn("未知的消息类型: {}", msgType);
                    break;
            }
        }
        return attachments;
    }

    /**
     * 任务维度发表统计（全部批次合计），无发表记录的任务不在返回中
     */
    @Override
    public List<WxMomentTaskStatVO> statByTaskIds(Collection<Long> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Collections.emptyList();
        }
        return wxMomentSendLogMapper.statByTaskIds(taskIds);
    }

    /**
     * 单任务发表统计：全部批次合计 + 各批次明细（含成员维度）
     */
    @Override
    public WxMomentTaskBatchStatResp statTaskBatches(Long taskId) {
        WxMomentTaskBatchStatResp resp = new WxMomentTaskBatchStatResp();
        resp.setTaskId(taskId);

        List<WxMomentBatchStatVO> batches = wxMomentSendLogMapper.statByTaskGroupInvoke(taskId);
        Map<Long, List<WxMomentUserStatVO>> userByInvoke = wxMomentSendLogMapper.statByTaskGroupInvokeUser(taskId)
                .stream()
                .collect(Collectors.groupingBy(WxMomentUserStatVO::getInvokeId));
        batches.forEach(batch -> batch.setMembers(userByInvoke.get(batch.getInvokeId())));
        resp.setBatches(batches);

        WxMomentTaskStatVO summary = new WxMomentTaskStatVO();
        summary.setTaskId(taskId);
        summary.setBatchCnt(batches.size());
        summary.setTotalCnt(batches.stream().mapToInt(b -> nz(b.getTotalCnt())).sum());
        summary.setPublishedCnt(batches.stream().mapToInt(b -> nz(b.getPublishedCnt())).sum());
        summary.setUnpublishedCnt(batches.stream().mapToInt(b -> nz(b.getUnpublishedCnt())).sum());
        resp.setSummary(summary);
        return resp;
    }

    private int nz(Integer value) {
        return value == null ? 0 : value;
    }
}
