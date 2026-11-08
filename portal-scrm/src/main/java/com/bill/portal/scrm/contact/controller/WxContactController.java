package com.bill.portal.scrm.contact.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.common.core.model.R;
import com.bill.portal.common.core.tenant.TenantContextHolder;
import com.bill.portal.scrm.common.enums.SyncType;
import com.bill.portal.scrm.common.enums.TriggerType;
import com.bill.portal.scrm.contact.dto.WxUserQuery;
import com.bill.portal.scrm.contact.entity.WxUser;
import com.bill.portal.scrm.contact.service.IWxContactSyncService;
import com.bill.portal.scrm.contact.service.IWxDepartmentService;
import com.bill.portal.scrm.contact.service.IWxUserService;
import com.bill.portal.scrm.contact.vo.WxDepartmentTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通讯录管理接口
 */
@RestController
@RequestMapping("/scrm/contact")
@Tag(name = "企微通讯录管理")
@AllArgsConstructor
public class WxContactController {

    private final IWxContactSyncService wxContactSyncService;

    private final IWxDepartmentService wxDepartmentService;

    private final IWxUserService wxUserService;

    @PostMapping("/sync/trigger")
    @SaCheckPermission("contact:sync")
    @Operation(summary = "手动触发通讯录全量同步")
    public R<?> triggerSync(@RequestParam("corpId") String corpId) {
        Long tenantId = TenantContextHolder.getTenantId();
        wxContactSyncService.executeFullSync(tenantId,corpId, SyncType.CONTACT_FULL.getCode(), TriggerType.MANUAL.getCode());
        return R.success();
    }

    @GetMapping("/deptTree")
    @Operation(summary = "部门树")
    public R<List<WxDepartmentTreeVO>> deptTree() {
        return R.success(wxDepartmentService.deptTree());
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public R<IPage<WxUser>> page(@ParameterObject Page<WxUser> page, @ParameterObject @Valid WxUserQuery query) {
        return R.success(wxUserService.page(page, query));
    }

    @GetMapping("/list")
    @Operation(summary = "列表查询")
    public R<List<WxUser>> list(@ParameterObject @Valid WxUserQuery query) {
        return R.success(wxUserService.list(query));
    }

}
