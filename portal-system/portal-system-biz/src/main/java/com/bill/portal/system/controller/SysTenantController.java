package com.bill.portal.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.oauth2.data.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.template.SaOAuth2Util;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.common.core.model.R;
import com.bill.portal.system.dto.SysTenantCreateReq;
import com.bill.portal.system.dto.SysTenantQuery;
import com.bill.portal.system.dto.SysTenantUpdateReq;
import com.bill.portal.system.entity.SysTenant;
import com.bill.portal.system.entity.SysUserTenant;
import com.bill.portal.system.mapper.SysUserTenantMapper;
import com.bill.portal.system.service.ISysTenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/system/tenant")
@Tag(name = "系统租户", description = "系统租户接口")
@RequiredArgsConstructor
public class SysTenantController {

    private final ISysTenantService sysTenantService;
    private final SysUserTenantMapper sysUserTenantMapper;

    @GetMapping("/list")
    @Operation(summary = "获取当前用户关联的租户列表")
    public R<List<SysTenant>> list() {
        AccessTokenModel at = SaOAuth2Util.currentAccessToken();
        if (at == null) {
            return R.fail("未登录");
        }
        Long userId = Long.valueOf(at.loginId.toString());

        // 查询用户关联的租户ID
        List<Long> tenantIds = sysUserTenantMapper.selectList(
                Wrappers.<SysUserTenant>lambdaQuery()
                        .eq(SysUserTenant::getUserId, userId))
                .stream()
                .map(SysUserTenant::getTenantId)
                .collect(Collectors.toList());

        if (tenantIds.isEmpty()) {
            return R.success(List.of());
        }

        // 查询启用的租户
        List<SysTenant> tenants = sysTenantService.list(
                Wrappers.<SysTenant>lambdaQuery()
                        .in(SysTenant::getId, tenantIds)
                        .eq(SysTenant::getStatus, 1)
                        .orderByAsc(SysTenant::getId));
        return R.success(tenants);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询", description = "分页查询")
    public R<IPage<SysTenant>> page(@ParameterObject Page<SysTenant> page,
                                  @ParameterObject SysTenantQuery query) {
        LambdaQueryWrapper<SysTenant> wrapper = Wrappers.<SysTenant>lambdaQuery()
                .like(StringUtils.hasText(query.getName()), SysTenant::getName, query.getName())
                .eq(Objects.nonNull(query.getStatus()), SysTenant::getStatus, query.getStatus());
        return R.success(sysTenantService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "通过id查询", description = "通过id查询")
    public R<SysTenant> getById(@PathVariable("id") Long id) {
        return R.success(sysTenantService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("#clientId:tenant:add")
    @Operation(summary = "新增", description = "新增")
    public R<Boolean> save(@RequestBody SysTenantCreateReq req) {
        SysTenant entity = BeanUtil.copyProperties(req, SysTenant.class);
        return R.success(sysTenantService.save(entity));
    }

    @PutMapping
    @SaCheckPermission("#clientId:tenant:update")
    @Operation(summary = "更新", description = "更新")
    public R<Boolean> updateById(@RequestBody SysTenantUpdateReq req) {
        SysTenant entity = BeanUtil.copyProperties(req, SysTenant.class);
        return R.success(sysTenantService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("#clientId:tenant:delete")
    @Operation(summary = "删除", description = "删除")
    public R<Boolean> removeById(@PathVariable("id") Long id) {
        return R.success(sysTenantService.removeById(id));
    }

    @DeleteMapping
    @SaCheckPermission("#clientId:tenant:delete")
    @Operation(summary = "批量删除", description = "批量删除")
    public R<Boolean> removeBatchByIds(@RequestParam("ids") String ids) {
        return R.success(sysTenantService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

}
