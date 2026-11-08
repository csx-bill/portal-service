package com.bill.portal.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.common.core.client.ClientContextHolder;
import com.bill.portal.common.core.model.R;
import com.bill.portal.system.dto.SysRoleCreateReq;
import com.bill.portal.system.dto.SysRoleQuery;
import com.bill.portal.system.dto.SysRoleUpdateReq;
import com.bill.portal.system.entity.SysRole;
import com.bill.portal.system.service.ISysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/system/role")
@Tag(name = "系统角色", description = "系统角色接口")
@RequiredArgsConstructor
public class SysRoleController {

    private final ISysRoleService sysRoleService;

    @GetMapping("/page")
    @Operation(summary = "分页查询", description = "分页查询")
    public R<IPage<SysRole>> page(@ParameterObject Page<SysRole> page,
                                  @ParameterObject @Valid SysRoleQuery query) {
        String clientId = ClientContextHolder.getClientId();
        LambdaQueryWrapper<SysRole> wrapper = Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getClientId,clientId)
                .like(StringUtils.hasText(query.getName()), SysRole::getName, query.getName())
                .eq(query.getStatus() != null, SysRole::getStatus, query.getStatus());
        return R.success(sysRoleService.pageWithRelations(page, wrapper));
    }

    @GetMapping("/list")
    @Operation(summary = "全部角色", description = "当前租户全部角色（下拉选项用）")
    public R<List<SysRole>> list() {
        return R.success(sysRoleService.list());
    }

    @GetMapping("/{id}")
    @Operation(summary = "通过id查询", description = "通过id查询")
    public R<SysRole> getById(@PathVariable("id") Long id) {
        return R.success(sysRoleService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("#clientId:role:add")
    @Operation(summary = "新增", description = "新增")
    public R<Boolean> save(@RequestBody @Valid SysRoleCreateReq req) {
        String clientId = ClientContextHolder.getClientId();
        SysRole entity = BeanUtil.copyProperties(req, SysRole.class);
        entity.setClientId(clientId);
        return R.success(sysRoleService.save(entity));
    }

    @PutMapping
    @SaCheckPermission("#clientId:role:update")
    @Operation(summary = "更新", description = "更新")
    public R<Boolean> updateById(@RequestBody @Valid SysRoleUpdateReq req) {
        SysRole entity = BeanUtil.copyProperties(req, SysRole.class);
        return R.success(sysRoleService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("#clientId:role:delete")
    @Operation(summary = "删除", description = "删除")
    public R<Boolean> removeById(@PathVariable("id") Long id) {
        return R.success(sysRoleService.removeById(id));
    }

    @DeleteMapping
    @SaCheckPermission("#clientId:role:delete")
    @Operation(summary = "批量删除", description = "批量删除")
    public R<Boolean> removeBatchByIds(@RequestParam("ids") String ids) {
        return R.success(sysRoleService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}
