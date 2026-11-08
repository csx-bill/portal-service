package com.bill.portal.scrm.welcome.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.common.core.model.R;
import com.bill.portal.scrm.welcome.dto.WxWelcomeMsgResp;
import com.bill.portal.scrm.welcome.entity.WxWelcomeMsg;
import com.bill.portal.scrm.welcome.req.WxWelcomeMsgQuery;
import com.bill.portal.scrm.welcome.req.WxWelcomeMsgSaveReq;
import com.bill.portal.scrm.welcome.service.IWxWelcomeMsgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 企微欢迎语管理
 */
@Tag(name = "企微欢迎语管理")
@RestController
@RequestMapping("/scrm/welcome")
@AllArgsConstructor
public class WxWelcomeMsgController {

    private final IWxWelcomeMsgService wxWelcomeMsgService;

    @GetMapping("/page")
    @Operation(summary = "分页查询欢迎语列表", description = "分页查询主欢迎语列表")
    public R<IPage<WxWelcomeMsg>> page(@ParameterObject Page page, @ParameterObject WxWelcomeMsgQuery query) {
        return R.success(wxWelcomeMsgService.page(page, query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "通过id查询", description = "欢迎语详情，含分时子项与绑定列表")
    public R<WxWelcomeMsgResp> getWelcomeMsgById(@PathVariable("id") Long id) {
        return R.success(wxWelcomeMsgService.getWelcomeMsgById(id));
    }

    @PostMapping
    @SaCheckPermission(value = {"welcome:add", "welcome:update"}, mode = SaMode.OR)
    @Operation(summary = "保存", description = "id 为空新增，否则更新；分时子项与绑定整组替换")
    public R<Boolean> saveWelcomeMsg(@RequestBody WxWelcomeMsgSaveReq params) {
        wxWelcomeMsgService.saveWelcomeMsg(params);
        return R.success();
    }

    @PostMapping("/bindConflicts")
    @Operation(summary = "绑定冲突预检", description = "返回与库中已有绑定时间重叠的冲突描述列表，空列表 = 无冲突")
    public R<List<String>> bindConflicts(@RequestBody WxWelcomeMsgSaveReq params) {
        return R.success(wxWelcomeMsgService.checkBindConflicts(params));
    }

    @DeleteMapping
    @SaCheckPermission("welcome:delete")
    @Operation(summary = "批量删除", description = "同时删除分时子项与绑定")
    public R<Boolean> delWelcomeMsgs(@RequestParam("ids") String ids) {
        wxWelcomeMsgService.delWelcomeMsgs(Arrays.asList(ids.split(",")));
        return R.success();
    }
}
