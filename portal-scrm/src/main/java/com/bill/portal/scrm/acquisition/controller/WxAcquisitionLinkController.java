package com.bill.portal.scrm.acquisition.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.common.core.model.R;
import com.bill.portal.common.core.tenant.TenantContextHolder;
import com.bill.portal.scrm.acquisition.dto.WxAcquisitionLinkCreateReq;
import com.bill.portal.scrm.acquisition.dto.WxAcquisitionLinkQuery;
import com.bill.portal.scrm.acquisition.dto.WxAcquisitionLinkUpdateReq;
import com.bill.portal.scrm.acquisition.entity.WxAcquisitionLink;
import com.bill.portal.scrm.acquisition.service.IWxAcquisitionLinkService;
import com.bill.portal.scrm.acquisition.service.IWxAcquisitionLinkSyncService;
import com.bill.portal.scrm.common.enums.SyncType;
import com.bill.portal.scrm.common.enums.TriggerType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/scrm/acquisition/link")
@Tag(name = "获客链接管理")
@RequiredArgsConstructor
public class WxAcquisitionLinkController {

    private final IWxAcquisitionLinkService wxAcquisitionLinkService;
    private final IWxAcquisitionLinkSyncService wxAcquisitionLinkSyncService;

    @GetMapping("/page")
    @Operation(summary = "分页查询", description = "分页查询")
    public R<IPage<WxAcquisitionLink>> page(@ParameterObject Page<WxAcquisitionLink> page,
                                            @ParameterObject WxAcquisitionLinkQuery query) {
        LambdaQueryWrapper<WxAcquisitionLink> wrapper = Wrappers.<WxAcquisitionLink>lambdaQuery()
                .like(StringUtils.hasText(query.getCorpId()), WxAcquisitionLink::getCorpId, query.getCorpId())
                .like(StringUtils.hasText(query.getLinkName()), WxAcquisitionLink::getLinkName, query.getLinkName())
                // 使用范围包含该成员/部门（两列均为 JSON 数组）
                .apply(StringUtils.hasText(query.getRangeUserid()),
                        "JSON_CONTAINS(range_user_list, JSON_QUOTE({0}))", query.getRangeUserid())
                .apply(query.getRangeDeptId() != null,
                        "JSON_CONTAINS(range_department_list, CAST({0} AS JSON))", query.getRangeDeptId());
        return R.success(wxAcquisitionLinkService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "通过id查询", description = "通过id查询")
    public R<WxAcquisitionLink> getById(@PathVariable("id") Long id) {
        return R.success(wxAcquisitionLinkService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("acquisition:add")
    @Operation(summary = "新增", description = "新增")
    public R<Boolean> save(@RequestBody WxAcquisitionLinkCreateReq req) throws WxErrorException {
        wxAcquisitionLinkSyncService.createLink(req);
        return R.success();
    }

    @PutMapping
    @SaCheckPermission("acquisition:update")
    @Operation(summary = "更新", description = "更新")
    public R<Boolean> updateById(@RequestBody WxAcquisitionLinkUpdateReq req) throws WxErrorException {
        wxAcquisitionLinkSyncService.updateLink(req);
        return R.success();
    }


    @DeleteMapping("/{id}")
    @SaCheckPermission("acquisition:delete")
    @Operation(summary = "删除", description = "删除")
    public R<Boolean> removeById(@PathVariable("id") Long id) throws WxErrorException {
        wxAcquisitionLinkSyncService.deleteLink(id);
        return R.success();
    }

    @DeleteMapping
    @SaCheckPermission("acquisition:delete")
    @Operation(summary = "批量删除", description = "批量删除")
    public R<Boolean> removeBatchByIds(@RequestParam("ids") String ids) throws WxErrorException {
        wxAcquisitionLinkSyncService.batchDeleteLink(ids);
        return R.success();
    }

    @PostMapping("/sync/trigger")
    @SaCheckPermission("acquisition:sync")
    @Operation(summary = "手动触发获客链接全量同步")
    public R<?> triggerSync(@RequestParam("corpId") String corpId) {
        Long tenantId = TenantContextHolder.getTenantId();
        wxAcquisitionLinkSyncService.executeFullSync(tenantId, corpId,
                SyncType.ACQUISITION_LINK_FULL.getCode(),
                TriggerType.MANUAL.getCode());
        return R.success();
    }

}
