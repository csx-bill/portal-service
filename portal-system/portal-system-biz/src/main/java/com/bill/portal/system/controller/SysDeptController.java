package com.bill.portal.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import com.bill.portal.common.core.model.R;
import com.bill.portal.system.dto.SysDeptCreateReq;
import com.bill.portal.system.dto.SysDeptUpdateReq;
import com.bill.portal.system.entity.SysDept;
import com.bill.portal.system.service.ISysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/dept")
@Tag(name = "系统部门", description = "系统部门接口")
@RequiredArgsConstructor
public class SysDeptController {

    private final ISysDeptService sysDeptService;

    @GetMapping("/tree")
    @Operation(summary = "部门树", description = "部门树")
    public R<List<SysDept>> tree() {
        return R.success(sysDeptService.tree());
    }

    @PostMapping
    @SaCheckPermission("#clientId:dept:add")
    @Operation(summary = "新增", description = "新增")
    public R<Boolean> save(@RequestBody @Valid SysDeptCreateReq req) {
        SysDept entity = BeanUtil.copyProperties(req, SysDept.class);
        return R.success(sysDeptService.save(entity));
    }

    @PutMapping
    @SaCheckPermission("#clientId:dept:update")
    @Operation(summary = "更新", description = "更新")
    public R<Boolean> updateById(@RequestBody @Valid SysDeptUpdateReq req) {
        SysDept entity = BeanUtil.copyProperties(req, SysDept.class);
        return R.success(sysDeptService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("#clientId:dept:delete")
    @Operation(summary = "删除", description = "删除")
    public R<Boolean> removeById(@PathVariable("id") Long id) {
        return R.success(sysDeptService.removeById(id));
    }
}
