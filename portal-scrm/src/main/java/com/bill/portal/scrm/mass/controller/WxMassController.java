package com.bill.portal.scrm.mass.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.common.core.exception.CheckedException;
import com.bill.portal.common.core.model.R;
import com.bill.portal.scrm.mass.dto.WxMassInvokeLogResp;
import com.bill.portal.scrm.mass.dto.WxMassTaskBatchStatResp;
import com.bill.portal.scrm.mass.dto.WxMassTaskStatVO;
import com.bill.portal.scrm.mass.entity.WxMassInvokeLog;
import com.bill.portal.scrm.mass.entity.WxMassTask;
import com.bill.portal.scrm.mass.req.WxMassInvokeLogQuery;
import com.bill.portal.scrm.mass.req.WxMassTaskQuery;
import com.bill.portal.scrm.mass.req.WxMassTaskSaveReq;
import com.bill.portal.scrm.mass.service.IWxMassService;
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
 * 企微群发管理
 */
@Tag(name = "企微群发管理")
@RestController
@RequestMapping("/scrm/mass")
@AllArgsConstructor
public class WxMassController {

    private final IWxMassService wxMassService;

    @GetMapping("/taskPage")
    @Operation(summary = "分页查询任务列表" , description = "分页查询任务列表" )
    public R<IPage<WxMassTask>> taskPage(@ParameterObject Page page, @ParameterObject @Valid WxMassTaskQuery query) {
        return R.success(wxMassService.taskPage(page,query));
    }

    @GetMapping("/invokeLogPage")
    @Operation(summary = "分页查询调用记录列表" , description = "分页查询调用记录列表" )
    public R<IPage<WxMassInvokeLog>> invokeLogPage(@ParameterObject Page page, @ParameterObject @Valid WxMassInvokeLogQuery query) {
        return R.success(wxMassService.invokeLogPage(page,query));
    }

    @GetMapping("/invokeLog/{id}")
    @Operation(summary = "通过id查询" , description = "通过id查询" )
    public R<WxMassInvokeLogResp> getInvokeById(@PathVariable("id") Long id) {
        return R.success(wxMassService.getInvokeById(id));
    }

    @DeleteMapping
    @SaCheckPermission("mass:delete")
    @Operation(summary = "批量删除" , description = "批量删除" )
    public R<Boolean> removeBatchByIds(@RequestParam("ids") String ids) {
        wxMassService.delTask(Arrays.asList(ids.split(",")));
        return R.success();
    }

    @PostMapping
    @SaCheckPermission(value = {"mass:add", "mass:update"}, mode = SaMode.OR)
    @Operation(summary = "保存" , description = "保存" )
    public R<Boolean> saveTask(@RequestBody WxMassTaskSaveReq params) {
        wxMassService.saveTask(params);
        return R.success();
    }

    @PostMapping("/syncResult")
    @SaCheckPermission("mass:sync")
    @Operation(summary = "同步企微群发任务结果" , description = "手动触发：同步近3天内已发送任务的送达结果，返回处理的批次数量" )
    public R<Integer> syncResult(@RequestParam("corpId") String corpId) {
        try {
            return R.success(wxMassService.executeSyncTaskResult(corpId, 3));
        } catch (WxErrorException e) {
            throw new CheckedException(e.getMessage());
        }
    }

    @GetMapping("/{id}" )
    @Operation(summary = "通过id查询" , description = "通过id查询" )
    public R<WxMassTaskSaveReq> getTaskById(@PathVariable("id") Long id) {
        return R.success(wxMassService.getTaskById(id));
    }

    @GetMapping("/statistic/taskList")
    @Operation(summary = "任务维度送达统计" , description = "按任务ID集合聚合 wx_mass_target_log，全部批次合计；无发送记录的任务不在返回中" )
    public R<List<WxMassTaskStatVO>> statTaskList(@RequestParam("ids") List<Long> ids) {
        return R.success(wxMassService.statByTaskIds(ids));
    }

    @GetMapping("/statistic/task/{taskId}")
    @Operation(summary = "单任务送达统计" , description = "返回全部批次合计与各批次（含成员维度）统计" )
    public R<WxMassTaskBatchStatResp> statTaskBatches(@PathVariable("taskId") Long taskId) {
        return R.success(wxMassService.statTaskBatches(taskId));
    }

}
