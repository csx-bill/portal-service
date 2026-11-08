package com.bill.portal.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.auth.dto.SysOauthClientCreateReq;
import com.bill.portal.auth.dto.SysOauthClientQuery;
import com.bill.portal.auth.dto.SysOauthClientUpdateReq;
import com.bill.portal.auth.entity.SysOauthClient;
import com.bill.portal.auth.service.ISysOauthClientService;
import com.bill.portal.common.core.client.ClientContextHolder;
import com.bill.portal.common.core.model.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/system/client")
@Tag(name = "客户端", description = "客户端接口")
@RequiredArgsConstructor
public class SysOauthClientController {
    private final ISysOauthClientService sysOauthClientService;

    @GetMapping("/page")
    @Operation(summary = "分页查询", description = "分页查询")
    public R<IPage<SysOauthClient>> page(@ParameterObject Page<SysOauthClient> page,
                                         @ParameterObject @Valid SysOauthClientQuery query) {
        LambdaQueryWrapper<SysOauthClient> wrapper = Wrappers.<SysOauthClient>lambdaQuery()
                .eq(StringUtils.hasText(query.getClientId()),SysOauthClient::getClientId,query.getClientId())
                .eq(query.getStatus() != null, SysOauthClient::getStatus, query.getStatus());
        return R.success(sysOauthClientService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "通过id查询", description = "通过id查询")
    public R<SysOauthClient> getById(@PathVariable("id") Long id) {
        return R.success(sysOauthClientService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("#clientId:client:add")
    @Operation(summary = "新增", description = "新增")
    public R<Boolean> save(@RequestBody @Valid SysOauthClientCreateReq req) {
        String clientId = ClientContextHolder.getClientId();
        SysOauthClient entity = BeanUtil.copyProperties(req, SysOauthClient.class);
        entity.setClientId(clientId);
        return R.success(sysOauthClientService.save(entity));
    }

    @PutMapping
    @SaCheckPermission("#clientId:client:update")
    @Operation(summary = "更新", description = "更新")
    public R<Boolean> updateById(@RequestBody @Valid SysOauthClientUpdateReq req) {
        SysOauthClient entity = BeanUtil.copyProperties(req, SysOauthClient.class);
        return R.success(sysOauthClientService.updateById(entity));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("#clientId:client:delete")
    @Operation(summary = "删除", description = "删除")
    public R<Boolean> removeById(@PathVariable("id") Long id) {
        return R.success(sysOauthClientService.removeById(id));
    }

    @DeleteMapping
    @SaCheckPermission("#clientId:client:delete")
    @Operation(summary = "批量删除", description = "批量删除")
    public R<Boolean> removeBatchByIds(@RequestParam("ids") String ids) {
        return R.success(sysOauthClientService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}
