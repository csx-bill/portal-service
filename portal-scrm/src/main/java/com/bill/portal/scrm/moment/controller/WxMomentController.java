package com.bill.portal.scrm.moment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.common.core.exception.CheckedException;
import com.bill.portal.common.core.model.R;
import com.bill.portal.scrm.mass.entity.WxMassTask;
import com.bill.portal.scrm.moment.dto.WxMomentInvokeLogResp;
import com.bill.portal.scrm.moment.dto.WxMomentTaskBatchStatResp;
import com.bill.portal.scrm.moment.dto.WxMomentTaskStatVO;
import com.bill.portal.scrm.moment.entity.WxMomentInvokeLog;
import com.bill.portal.scrm.moment.entity.WxMomentTask;
import com.bill.portal.scrm.moment.req.WxMomentInvokeLogQueryReq;
import com.bill.portal.scrm.moment.req.WxMomentTaskQueryReq;
import com.bill.portal.scrm.moment.req.WxMomentTaskSaveReq;
import com.bill.portal.scrm.moment.service.IWxMomentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 企微朋友圈管理
 */
@Tag(name = "企微朋友圈管理")
@RestController
@RequestMapping("/scrm/moment")
@AllArgsConstructor
public class WxMomentController {

    private final IWxMomentService wxMomentService;

    @GetMapping("/taskPage")
    @Operation(summary = "分页查询任务列表" , description = "分页查询任务列表" )
    public R<IPage<WxMomentTask>> taskPage(@ParameterObject Page page, @ParameterObject @Valid WxMomentTaskQueryReq params) {
        return R.success(wxMomentService.taskPage(page,params));
    }

    @GetMapping("/invokeLogPage")
    @Operation(summary = "分页查询调用记录列表" , description = "分页查询调用记录列表" )
    public R<IPage<WxMomentInvokeLog>> invokeLogPage(@ParameterObject Page page, @ParameterObject @Valid WxMomentInvokeLogQueryReq params) {
        return R.success(wxMomentService.invokeLogPage(page,params));
    }

    @GetMapping("/invokeLog/{id}")
    @Operation(summary = "通过id查询" , description = "通过id查询" )
    public R<WxMomentInvokeLogResp> getInvokeById(@PathVariable("id") Long id) {
        return R.success(wxMomentService.getInvokeById(id));
    }

    @DeleteMapping
    @SaCheckPermission("moment:delete")
    @Operation(summary = "批量删除" , description = "批量删除" )
    public R<Boolean> removeBatchByIds(@RequestParam("ids") String ids) {
        wxMomentService.delTask(Arrays.asList(ids.split(",")));
        return R.success();
    }

    @PostMapping
    @SaCheckPermission(value = {"moment:add", "moment:update"}, mode = SaMode.OR)
    @Operation(summary = "保存" , description = "保存" )
    public R<Boolean> saveTask(@RequestBody WxMomentTaskSaveReq params) {
        wxMomentService.saveTask(params);
        return R.success();
    }

    @GetMapping("/{id}" )
    @Operation(summary = "通过id查询" , description = "通过id查询" )
    public R<WxMomentTaskSaveReq> getTaskById(@PathVariable("id") Long id) {
        return R.success(wxMomentService.getTaskById(id));
    }

    @GetMapping("/statistic/taskList")
    @Operation(summary = "任务维度发表统计" , description = "按任务ID集合聚合 wx_moment_send_log，全部批次合计；无发表记录的任务不在返回中" )
    public R<List<WxMomentTaskStatVO>> statTaskList(@RequestParam("ids") List<Long> ids) {
        return R.success(wxMomentService.statByTaskIds(ids));
    }

    @PostMapping("/syncResult")
    @SaCheckPermission("moment:sync")
    @Operation(summary = "同步企微朋友圈任务结果" , description = "手动触发：同步近3天内未完成任务的创建状态与逐成员发表结果，返回处理的批次数量" )
    public R<Integer> syncResult(@RequestParam("corpId") String corpId) {
        try {
            return R.success(wxMomentService.executeSyncTaskResult(corpId, 3));
        } catch (WxErrorException e) {
            throw new CheckedException(e.getMessage());
        }
    }

    @GetMapping("/statistic/task/{taskId}")
    @Operation(summary = "单任务发表统计" , description = "返回全部批次合计与各批次（含成员维度）统计" )
    public R<WxMomentTaskBatchStatResp> statTaskBatches(@PathVariable("taskId") Long taskId) {
        return R.success(wxMomentService.statTaskBatches(taskId));
    }

}
