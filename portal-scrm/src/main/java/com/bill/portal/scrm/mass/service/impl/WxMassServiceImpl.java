package com.bill.portal.scrm.mass.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.common.core.exception.CheckedException;
import com.bill.portal.common.core.scheduler.ScheduleType;
import com.bill.portal.common.core.scheduler.SchedulerEngine;
import com.bill.portal.scrm.mass.dto.WxMassInvokeLogResp;
import com.bill.portal.scrm.mass.dto.WxMassBatchStatVO;
import com.bill.portal.scrm.mass.dto.WxMassTaskBatchStatResp;
import com.bill.portal.scrm.mass.dto.WxMassTaskStatVO;
import com.bill.portal.scrm.mass.dto.WxMassUserStatVO;
import com.bill.portal.scrm.mass.entity.*;
import com.bill.portal.scrm.mass.mapper.*;
import com.bill.portal.scrm.mass.req.WxMassInvokeLogQuery;
import com.bill.portal.scrm.mass.req.WxMassTaskQuery;
import com.bill.portal.scrm.service.IWxMediaService;
import com.google.gson.Gson;
import com.bill.portal.scrm.common.constant.WxMassConstants;
import com.bill.portal.scrm.contact.entity.WxUser;
import com.bill.portal.scrm.contact.mapper.WxUserMapper;
import com.bill.portal.scrm.contact.service.IWxUserService;
import com.bill.portal.scrm.corp.strategy.WxCorpApiStrategy;
import com.bill.portal.scrm.mass.req.WxMassContentSaveReq;
import com.bill.portal.scrm.mass.req.WxMassTaskSaveReq;
import com.bill.portal.scrm.mass.service.IWxMassService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.WxCpExternalContactService;
import me.chanjar.weixin.cp.bean.external.WxCpMsgTemplate;
import me.chanjar.weixin.cp.bean.external.WxCpMsgTemplateAddResult;
import me.chanjar.weixin.cp.bean.external.contact.WxCpGroupMsgSendResult;
import me.chanjar.weixin.cp.bean.external.msg.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 企微群发核心服务
 */
@Slf4j
@Service
@AllArgsConstructor
public class WxMassServiceImpl implements IWxMassService {

    private final WxCorpApiStrategy wxCorpApiStrategy;

    private final WxMassTaskMapper wxMassTaskMapper;

    private final WxMassTargetMapper wxMassTargetMapper;

    private final WxMassTargetLogMapper wxMassTargetLogMapper;

    private final WxMassContentMapper wxMassContentMapper;

    private final WxMassInvokeLogMapper wxMassInvokeLogMapper;

    private final WxMassContentLogMapper wxMassContentLogMapper;

    private final IWxMediaService wxMediaService;

    private final WxUserMapper wxUserMapper;

    private final IWxUserService wxUserService;

    private final SchedulerEngine engine;


    /**
     * 保存群发任务
     * @param req
     */
    @Override
    public void saveTask(WxMassTaskSaveReq req) {
        // 参数校验保持不变
        String chatType = req.getChatType();
        if(chatType.equals(WxMassConstants.CHAT_TYPE_SINGLE)){
            List<String> externalUserIdList = req.getTargetIdList();
            if(CollectionUtils.isEmpty(externalUserIdList)){
                throw new CheckedException("客户不能为空");
            }
            if(externalUserIdList.size() > 10000){
                throw new CheckedException("单聊群发客户数量不能超过1万个");
            }
        }
        if(chatType.equals(WxMassConstants.CHAT_TYPE_GROUP)){
            List<String> chatIdList = req.getTargetIdList();
            if(CollectionUtils.isEmpty(chatIdList)){
                throw new CheckedException("群不能为空");
            }
            if(chatIdList.size() > 2000){
                throw new CheckedException("群聊群发群数量不能超过2000个");
            }
            if(CollectionUtils.isEmpty(req.getSenderList())){
                throw new CheckedException("发送成员不能为空");
            }
        }
        if(chatType.equals(WxMassConstants.CHAT_TYPE_TAG)){
            // tagFilter 存储的格式 每组最多100个标签
            //            {
            //                "group_list":
            //                [
            //                        {
            //                            "tag_list":["ete19278asuMT123109rBAAAA","ete19MT12278109UYteaBAAAA"]
            //                        },
            //                        {
            //                            "tag_list":["eteIlKKHSDfuMT18Kg9rBAAAA"]
            //                        }
            //                ]
            //            }
            if(StringUtils.isEmpty(req.getTagFilter())){
                throw new CheckedException("标签筛选条件不能为空");
            }
        }
        if(CollectionUtils.isEmpty(req.getContentList())){
            throw new CheckedException("群发内容不能为空");
        }

        // 转换任务实体
        WxMassTask wxMassTask = BeanUtil.copyProperties(req, WxMassTask.class);

        // 处理立即执行的时间
        if (ScheduleType.IMMEDIATE.equals(req.getExecuteType())) {
            // 如果是立即执行，将 scheduled_time 设置为当前时间
            wxMassTask.setScheduledTime(LocalDateTime.now());
        }

        // 保存或更新任务
        if (req.getId() != null) {
            // 更新任务
            wxMassTaskMapper.updateById(wxMassTask);
            // 删除原有的目标和内容
            wxMassTargetMapper.delete(Wrappers.<WxMassTarget>lambdaQuery().eq(WxMassTarget::getTaskId, req.getId()));
            wxMassContentMapper.delete(Wrappers.<WxMassContent>lambdaQuery().eq(WxMassContent::getTaskId, req.getId()));
        } else {
            // 新增任务
            wxMassTaskMapper.insert(wxMassTask);
        }

        // 保存目标列表
        List<WxMassTarget> wxMassTargetList = req.getTargetIdList().stream()
                .map(targetId -> {
                    WxMassTarget target = new WxMassTarget();
                    target.setTaskId(wxMassTask.getId());
                    target.setChatType(chatType);
                    target.setTargetId(targetId);
                    return target;
                })
                .collect(Collectors.toList());
        wxMassTargetMapper.insert(wxMassTargetList);

        // 保存内容列表
        List<WxMassContent> wxMassContentList = req.getContentList().stream()
                .map(content -> {
                    WxMassContent wxMassContent = BeanUtil.copyProperties(content, WxMassContent.class);
                    wxMassContent.setTaskId(wxMassTask.getId());
                    return wxMassContent;
                })
                .collect(Collectors.toList());
        wxMassContentMapper.insert(wxMassContentList);

        if (wxMassTask.getScheduleType().isRecurring()) {
            engine.schedule(wxMassTask.getId());
        }
    }

    /**
     * 删除群发任务
     * @param idList
     */
    @Override
    public void delTask(Collection<?> idList) {
        for (Object id : idList) {
            // controller 传入的是字符串形式的 id，需转换后才能取消调度
            engine.cancel(Long.valueOf(id.toString()));
        }
        wxMassTaskMapper.deleteByIds(idList);
    }

    @Override
    public WxMassTaskSaveReq getTaskById(Long id) {
        WxMassTask wxMassTask = wxMassTaskMapper.selectById(id);
        WxMassTaskSaveReq req = BeanUtil.copyProperties(wxMassTask, WxMassTaskSaveReq.class);
        // 查询任务目标列表
        List<WxMassTarget> targets = wxMassTargetMapper.selectList(
                Wrappers.<WxMassTarget>lambdaQuery().eq(WxMassTarget::getTaskId, id)
        );
        req.setTargetIdList(targets.stream()
                .map(WxMassTarget::getTargetId)
                .collect(Collectors.toList())
        );

        // 查询任务内容列表并转换为WxMassContentCreateReq
        List<WxMassContent> contents = wxMassContentMapper.selectList(
                Wrappers.<WxMassContent>lambdaQuery().eq(WxMassContent::getTaskId, id)
        );
        req.setContentList(contents.stream()
                .map(content -> {
                    return BeanUtil.copyProperties(content, WxMassContentSaveReq.class);
                })
                .collect(Collectors.toList())
        );

        return req;
    }

    @Override
    public IPage<WxMassTask> taskPage(Page<WxMassTask> page, WxMassTaskQuery query) {
        // 构建查询条件
        LambdaQueryWrapper<WxMassTask> queryWrapper = Wrappers.<WxMassTask>lambdaQuery()
                .eq(StringUtils.hasText(query.getCorpId()),WxMassTask::getCorpId, query.getCorpId())
                .eq(StringUtils.hasText(query.getChatType()),WxMassTask::getChatType, query.getChatType())
                .eq(Objects.nonNull(query.getStatus()),WxMassTask::getStatus, query.getStatus())
                .like(StringUtils.hasText(query.getTaskName()),WxMassTask::getTaskName, query.getTaskName())
                // sender_list 为 JSON 数组字符串，按成员过滤用 JSON_CONTAINS 精确匹配
                .apply(StringUtils.hasText(query.getSender()),
                        "JSON_CONTAINS(sender_list, JSON_QUOTE({0}))", query.getSender());

        // 部门过滤：命中部门及子部门下的发送成员（sender_list 与成员集合有交集）
        if (query.getDeptId() != null) {
            List<String> deptUserIds = wxUserService
                    .listUserIdsByDeptTree(query.getCorpId(), query.getDeptId());
            if (deptUserIds.isEmpty()) {
                queryWrapper.apply("1=0");
            } else {
                queryWrapper.apply("JSON_OVERLAPS(sender_list, CAST({0} AS JSON))",
                        new Gson().toJson(deptUserIds));
            }
        }

        queryWrapper.orderByDesc(WxMassTask::getCreateTime);

        return wxMassTaskMapper.selectPage(page,queryWrapper);
    }

    @Override
    public IPage<WxMassInvokeLog> invokeLogPage(Page<WxMassInvokeLog> page, WxMassInvokeLogQuery query) {
        // 构建查询条件
        LambdaQueryWrapper<WxMassInvokeLog> queryWrapper = Wrappers.<WxMassInvokeLog>lambdaQuery()
                .eq(StringUtils.hasText(query.getCorpId()),WxMassInvokeLog::getCorpId, query.getCorpId())
                .eq(Objects.nonNull(query.getTaskId()),WxMassInvokeLog::getTaskId, query.getTaskId())
                .eq(StringUtils.hasText(query.getChatType()),WxMassInvokeLog::getChatType, query.getChatType())
                .eq(Objects.nonNull(query.getStatus()),WxMassInvokeLog::getStatus, query.getStatus())
                .like(StringUtils.hasText(query.getTaskName()),WxMassInvokeLog::getTaskName, query.getTaskName())
                .orderByDesc(WxMassInvokeLog::getCreateTime);
        return wxMassInvokeLogMapper.selectPage(page,queryWrapper);
    }

    @Override
    public WxMassInvokeLogResp getInvokeById(Long id) {
        WxMassInvokeLog wxMassInvokeLog = wxMassInvokeLogMapper.selectById(id);
        if (wxMassInvokeLog == null) {
            throw new CheckedException("调用记录不存在");
        }
        WxMassInvokeLogResp resp = BeanUtil.copyProperties(wxMassInvokeLog, WxMassInvokeLogResp.class);

        // 查询任务目标列表
        List<WxMassTargetLog> targets = wxMassTargetLogMapper.selectList(
                Wrappers.<WxMassTargetLog>lambdaQuery().eq(WxMassTargetLog::getInvokeId, id)
        );
        resp.setTargetIdList(targets.stream()
                .map(WxMassTargetLog::getTargetId)
                .collect(Collectors.toList())
        );

        // 查询任务内容列表并转换为WxMassContentSaveReq
        List<WxMassContentLog> contents = wxMassContentLogMapper.selectList(
                Wrappers.<WxMassContentLog>lambdaQuery().eq(WxMassContentLog::getInvokeId, id)
        );
        resp.setContentList(contents.stream()
                .map(content -> {
                    return BeanUtil.copyProperties(content, WxMassContentSaveReq.class);
                })
                .collect(Collectors.toList())
        );
        return resp;
    }

    /**
     * 执行任务
     * @param wxMassTask 群发任务
     * @param triggerType 触发类型
     */
    @Override
    public void executeTask(WxMassTask wxMassTask, String triggerType, String batchNo) throws IOException, WxErrorException {
        List<String> senderList = wxMassTask.getSenderList();
        // 预先构建并上传附件资源
        List<Attachment> attachments = buildAndUploadAttachments(wxMassTask);

        if(CollectionUtils.isEmpty(senderList)){
            executeTaskWithLog(wxMassTask, triggerType, null, attachments, batchNo);
        }else {
            for (String sender : senderList) {
                executeTaskWithLog(wxMassTask, triggerType, sender, attachments, batchNo);
            }
        }
    }


    /**
     * 执行单个群发任务并记录日志
     * @param task 群发任务实体
     * @param triggerType 触发类型
     * @param sender 发送者
     */
    @Override
    public void executeTaskWithLog(WxMassTask task,String triggerType,String sender,List<Attachment> attachments, String batchNo){
        WxMassInvokeLog invokeLog = BeanUtil.copyProperties(task, WxMassInvokeLog.class);
        invokeLog.setId(null);
        invokeLog.setTaskId(task.getId());
        invokeLog.setTriggerType(triggerType);
        invokeLog.setStatus("SUCCESS");
        invokeLog.setSender(sender);
        invokeLog.setBatchNo(batchNo);
        try {
            WxCpExternalContactService externalContactService = wxCorpApiStrategy.getWxCpService(task.getCorpId()).getExternalContactService();

            WxCpMsgTemplate wxCpMsgTemplate = new WxCpMsgTemplate();
            wxCpMsgTemplate.setSender(sender);

            // 查询群发目标
            List<WxMassTarget> wxMassTargets = wxMassTargetMapper.selectList(Wrappers.<WxMassTarget>lambdaQuery().eq(WxMassTarget::getTaskId, task.getId()));

            List<String> targetIdList = wxMassTargets.stream().map(WxMassTarget::getTargetId).collect(Collectors.toList());

            // 根据群发类型设置不同的目标参数
            switch (task.getChatType()) {
                case "single":
                    wxCpMsgTemplate.setChatType(task.getChatType());
                    wxCpMsgTemplate.setExternalUserid(targetIdList);
                    break;
                case "group":
                    wxCpMsgTemplate.setChatType(task.getChatType());
                    wxCpMsgTemplate.setChatIdList(targetIdList);
                    break;
                case "tag":
                    wxCpMsgTemplate.setChatType("single");
                    Gson gson = new Gson();
                    wxCpMsgTemplate.setTagFilter(gson.fromJson(task.getTagFilter(), TagFilter.class));
                    break;
                default:
                    break;
            }

            wxCpMsgTemplate.setAllowSelect(task.isAllowSelect());

            // 处理文本消息（文本无需上传，每次直接设置即可）
            List<WxMassContent> wxMassContents = wxMassContentMapper.selectList(Wrappers.<WxMassContent>lambdaQuery()
                    .eq(WxMassContent::getTaskId, task.getId())
            );
            for (WxMassContent wxMassContent : wxMassContents) {
                if ("text".equals(wxMassContent.getMsgType())) {
                    Text text = new Text();
                    text.setContent(wxMassContent.getTextContent());
                    wxCpMsgTemplate.setText(text);
                    break;
                }
            }

            // 设置附件列表
            wxCpMsgTemplate.setAttachments(attachments);
            // 调用企微API发送群发消息
            WxCpMsgTemplateAddResult wxCpMsgTemplateAddResult = externalContactService.addMsgTemplate(wxCpMsgTemplate);
            invokeLog.setMsgid(wxCpMsgTemplateAddResult.getMsgId());
            invokeLog.setExtraData(wxCpMsgTemplateAddResult.toJson());

            // 1. 保存调用日志
            wxMassInvokeLogMapper.insert(invokeLog);
            // 2. 提取循环外的不变参数，避免在循环中重复调用 getter
            final Long taskId = task.getId();
            final Long invokeId = invokeLog.getId();

            // 3. 使用 Stream 进行转换和批量赋值
            List<WxMassContentLog> wxMassContentLogs = wxMassContents.stream()
                    .map(content -> {
                        WxMassContentLog log = BeanUtil.copyProperties(content, WxMassContentLog.class);
                        log.setId(null);
                        log.setTaskId(taskId);
                        log.setInvokeId(invokeId);
                        return log;
                    })
                    // 提前指定 ArrayList 大小，避免扩容消耗
                    .collect(Collectors.toCollection(() -> new ArrayList<>(wxMassContents.size())));

            // 4. 批量保存
            wxMassContentLogMapper.insert(wxMassContentLogs);

            // 记录发送失败的目标
            List<String> failList = wxCpMsgTemplateAddResult.getFailList();
            if (!CollectionUtils.isEmpty(failList)) {
                List<WxMassTargetLog> logs = failList.stream()
                        .map(id -> {
                            WxMassTargetLog log = new WxMassTargetLog();
                            log.setTaskId(task.getId());
                            log.setInvokeId(invokeLog.getId());
                            log.setChatType(task.getChatType());
                            log.setStatus(3);
                            log.setTargetId(id);
                            log.setTenantId(task.getTenantId());
                            log.setCreateTime(LocalDateTime.now());
                            log.setUpdateTime(LocalDateTime.now());
                            return log;
                        })
                        .collect(Collectors.toList());
                wxMassTargetLogMapper.batchUpsert(logs);
            }

        }catch (Exception e){
            invokeLog.setStatus("FAIL");
            invokeLog.setErrmsg(e.getMessage());
            wxMassInvokeLogMapper.insert(invokeLog);
        }

    }

    @Override
    public void executeFullSyncTaskResult(String corpId) throws WxErrorException {
        this.executeSyncTaskResult(corpId, 3);
    }

    /**
     * 同步指定时间窗内已发送任务的送达结果
     * @param corpId 企业ID
     * @param days 时间窗口（天，只处理窗口内创建的调用）
     * @return 处理的批次（调用记录）数量
     */
    @Override
    public int executeSyncTaskResult(String corpId, int days) throws WxErrorException {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<WxMassInvokeLog> invokeLogs = wxMassInvokeLogMapper.selectList(
                Wrappers.<WxMassInvokeLog>lambdaQuery()
                        .eq(WxMassInvokeLog::getCorpId,corpId)
                        .eq(WxMassInvokeLog::getStatus, "SUCCESS")
                        .ge(WxMassInvokeLog::getCreateTime, since)
                        .isNotNull(WxMassInvokeLog::getMsgid)
        );

        List<WxUser> wxUsers = wxUserMapper.selectList(
                Wrappers.<WxUser>lambdaQuery()
                        .eq(WxUser::getCorpId, corpId)
        );

        for (WxMassInvokeLog invokeLog : invokeLogs) {
            for (WxUser wxUser : wxUsers) {
                this.syncTaskResult(invokeLog,wxUser.getUserId());
            }
        }
        return invokeLogs.size();
    }

    @Override
    public void syncTaskResult(WxMassInvokeLog invokeLog,String userId) throws WxErrorException {

        WxCpExternalContactService externalContactService = wxCorpApiStrategy.getWxCpService(invokeLog.getCorpId()).getExternalContactService();

        String cursor = null;
        boolean hasMore = true;

        // 3. 循环使用游标拉取数据，直到没有下一页
        while (hasMore) {
            WxCpGroupMsgSendResult result = externalContactService.getGroupMsgSendResult(invokeLog.getMsgid(), userId, 1000, cursor);

            if (result != null && result.getSendList() != null) {
                List<WxMassTargetLog> list = new ArrayList<>();

                for (WxCpGroupMsgSendResult.ExternalContactGroupMsgSendInfo detail : result.getSendList()) {
                    String chatType = invokeLog.getChatType();

                    WxMassTargetLog targetLog = new WxMassTargetLog();
                    targetLog.setTaskId(invokeLog.getTaskId());
                    targetLog.setInvokeId(invokeLog.getId());
                    targetLog.setChatType(chatType);
                    targetLog.setUserId(userId);
                    targetLog.setStatus(detail.getStatus());
                    targetLog.setTenantId(invokeLog.getTenantId());
                    targetLog.setCreateTime(LocalDateTime.now());
                    targetLog.setUpdateTime(LocalDateTime.now());
                    // 将秒级时间戳(Long)转换为 LocalDateTime
                    if (Objects.nonNull(detail.getSendTime())) {
                        targetLog.setSendTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(detail.getSendTime()), ZoneId.systemDefault()));
                    }

                    switch (chatType) {
                        case "single":
                            targetLog.setTargetId(detail.getExternalUserId());
                            break;
                        case "group":
                            targetLog.setTargetId(detail.getChatId());
                            break;
                        case "tag":
                            targetLog.setTargetId(detail.getExternalUserId());
                            break;
                        default:
                            break;
                    }
                    list.add(targetLog);
                }

                // 批量更新状态
                if (!list.isEmpty()) {
                    wxMassTargetLogMapper.batchUpsert(list);
                }
            }

            // 获取下一页的游标
            cursor = result.getNextCursor();
            // 若游标为空，说明已拉取完毕，退出循环
            hasMore = cursor != null && !cursor.isEmpty();
        }

    }

    /**
     * 预先构建并上传附件资源，避免多发送者重复上传
     * @param task 群发任务实体
     * @return 附件列表
     */
    private List<Attachment> buildAndUploadAttachments(WxMassTask task) throws IOException, WxErrorException {
        List<WxMassContent> wxMassContents = wxMassContentMapper.selectList(
                Wrappers.<WxMassContent>lambdaQuery().eq(WxMassContent::getTaskId, task.getId())
        );
        List<Attachment> attachments = new ArrayList<>();
        for (WxMassContent wxMassContent : wxMassContents) {
            Attachment attachment = new Attachment();
            String msgType = wxMassContent.getMsgType();
            String url = wxMassContent.getUrl();

            switch (msgType) {
                case "image":
                    Image image = new Image();
                    String imageMediaId = wxMediaService.uploadTmpMedia(task.getCorpId(), msgType, wxMassContent.getFilename(), url);
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
                    String videoMediaId = wxMediaService.uploadTmpMedia(task.getCorpId(), msgType, wxMassContent.getFilename(), url);
                    video.setMediaId(videoMediaId);
                    attachment.setVideo(video);
                    attachments.add(attachment);
                    break;
                case "file":
                    File file = new File();
                    String fileMediaId = wxMediaService.uploadTmpMedia(task.getCorpId(), msgType, wxMassContent.getFilename(), url);
                    file.setMediaId(fileMediaId);
                    attachment.setFile(file);
                    attachments.add(attachment);
                    break;
                case "miniprogram":
                    MiniProgram miniProgram = new MiniProgram();
                    String miniMediaId = wxMediaService.uploadTmpMedia(task.getCorpId(), msgType, wxMassContent.getFilename(), url);
                    miniProgram.setPicMediaId(miniMediaId);
                    miniProgram.setTitle(wxMassContent.getMiniTitle());
                    miniProgram.setPage(wxMassContent.getMiniPage());
                    miniProgram.setAppid(wxMassContent.getMiniAppid());
                    attachment.setMiniProgram(miniProgram);
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
     * 任务维度送达统计（全部批次合计），无发送记录的任务不在返回中
     */
    @Override
    public List<WxMassTaskStatVO> statByTaskIds(Collection<Long> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Collections.emptyList();
        }
        return wxMassTargetLogMapper.statByTaskIds(taskIds);
    }

    /**
     * 单任务送达统计：全部批次合计 + 各批次明细（含成员维度）
     */
    @Override
    public WxMassTaskBatchStatResp statTaskBatches(Long taskId) {
        WxMassTaskBatchStatResp resp = new WxMassTaskBatchStatResp();
        resp.setTaskId(taskId);

        List<WxMassBatchStatVO> batches = wxMassTargetLogMapper.statByTaskGroupInvoke(taskId);
        // 成员明细按批次挂载（invokeId 分组）
        Map<Long, List<WxMassUserStatVO>> userByInvoke = wxMassTargetLogMapper.statByTaskGroupInvokeUser(taskId)
                .stream()
                .collect(Collectors.groupingBy(WxMassUserStatVO::getInvokeId));
        batches.forEach(batch -> batch.setMembers(userByInvoke.get(batch.getInvokeId())));
        resp.setBatches(batches);

        // 全部批次合计（成员明细不汇总，前端按所选批次展示）
        WxMassTaskStatVO summary = new WxMassTaskStatVO();
        summary.setTaskId(taskId);
        summary.setBatchCnt(batches.size());
        summary.setTotalCnt(batches.stream().mapToInt(b -> nz(b.getTotalCnt())).sum());
        summary.setDeliveredCnt(batches.stream().mapToInt(b -> nz(b.getDeliveredCnt())).sum());
        summary.setUnsentCnt(batches.stream().mapToInt(b -> nz(b.getUnsentCnt())).sum());
        summary.setFailFriendCnt(batches.stream().mapToInt(b -> nz(b.getFailFriendCnt())).sum());
        summary.setFailOtherCnt(batches.stream().mapToInt(b -> nz(b.getFailOtherCnt())).sum());
        resp.setSummary(summary);
        return resp;
    }

    private int nz(Integer value) {
        return value == null ? 0 : value;
    }


}
