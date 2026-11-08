package com.bill.portal.scrm.userbinding.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bill.portal.common.core.model.R;
import com.bill.portal.scrm.userbinding.dto.WxUserBindingSaveReq;
import com.bill.portal.scrm.userbinding.service.IWxUserBindingService;
import com.bill.portal.scrm.userbinding.vo.WxUserBindingVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统用户-企微成员绑定（数据权限前置：
 * 通讯录页从企微成员侧手动绑定系统账号，绑定后业务数据
 * 才能沿 成员→账号→系统部门 链路按角色 data_scope 过滤）
 */
@Tag(name = "企微成员绑定")
@Validated
@RestController
@RequestMapping("/scrm/user-binding")
@AllArgsConstructor
public class WxUserBindingController {

    private final IWxUserBindingService wxUserBindingService;

    @GetMapping("/list")
    @Operation(summary = "绑定列表", description = "当前租户全部绑定，带成员姓名与账号信息")
    public R<List<WxUserBindingVo>> list() {
        return R.success(wxUserBindingService.list());
    }

    @PostMapping
    @SaCheckPermission("userbinding:bind")
    @Operation(summary = "绑定/换绑", description = "租户内 账号↔成员 双向 1:1；同一账号重复绑定视为换绑，成员被其他账号绑定则拒绝")
    public R<Boolean> bind(@Valid @RequestBody WxUserBindingSaveReq req) {
        wxUserBindingService.bind(req);
        return R.success(true);
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("userbinding:unbind")
    @Operation(summary = "解绑")
    public R<Boolean> unbind(@NotNull @PathVariable("id") Long id) {
        wxUserBindingService.unbind(id);
        return R.success(true);
    }
}
