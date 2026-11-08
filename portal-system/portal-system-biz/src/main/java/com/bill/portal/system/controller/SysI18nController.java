package com.bill.portal.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.common.core.client.ClientContextHolder;
import com.bill.portal.common.core.model.R;
import com.bill.portal.system.dto.SysI18nCreateReq;
import com.bill.portal.system.dto.SysI18nQuery;
import com.bill.portal.system.dto.SysI18nUpdateReq;
import com.bill.portal.system.entity.SysI18n;
import com.bill.portal.system.service.ISysI18nService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/system/i18n")
@Tag(name = "国际化管理", description = "国际化管理接口")
@RequiredArgsConstructor
public class SysI18nController {

    private final ISysI18nService sysI18nService;

    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public R<IPage<SysI18n>> page(@ParameterObject Page<SysI18n> page,
                                  @ParameterObject @Valid SysI18nQuery query) {
        String clientId = ClientContextHolder.getClientId();
        LambdaQueryWrapper<SysI18n> wrapper = Wrappers.<SysI18n>lambdaQuery()
                .eq(SysI18n::getClientId,clientId)
                .like(StringUtils.hasText(query.getName()), SysI18n::getName, query.getName())
                .orderByAsc(SysI18n::getName);
        return R.success(sysI18nService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "通过id查询")
    public R<SysI18n> getById(@PathVariable("id") Long id) {
        return R.success(sysI18nService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("#clientId:i18n:add")
    @Operation(summary = "新增")
    public R<Boolean> save(@RequestBody @Valid SysI18nCreateReq params) {
        String clientId = ClientContextHolder.getClientId();
        SysI18n sysI18n = BeanUtil.copyProperties(params, SysI18n.class);
        sysI18n.setClientId(clientId);
        return R.success(sysI18nService.save(sysI18n));
    }

    @PutMapping
    @SaCheckPermission("#clientId:i18n:update")
    @Operation(summary = "更新")
    public R<Boolean> updateById(@RequestBody @Valid SysI18nUpdateReq params) {
        SysI18n sysI18n = BeanUtil.copyProperties(params, SysI18n.class);
        return R.success(sysI18nService.updateById(sysI18n));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("#clientId:i18n:delete")
    @Operation(summary = "删除")
    public R<Boolean> removeById(@PathVariable("id") Long id) {
        return R.success(sysI18nService.removeById(id));
    }

    @DeleteMapping
    @SaCheckPermission("#clientId:i18n:delete")
    @Operation(summary = "批量删除")
    public R<Boolean> removeBatchByIds(@RequestParam("ids") String ids) {
        return R.success(sysI18nService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}
