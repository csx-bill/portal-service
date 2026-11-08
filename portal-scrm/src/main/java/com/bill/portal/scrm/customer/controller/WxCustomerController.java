package com.bill.portal.scrm.customer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.common.core.model.R;
import com.bill.portal.common.core.tenant.TenantContextHolder;
import com.bill.portal.scrm.common.enums.SyncType;
import com.bill.portal.scrm.common.enums.TriggerType;
import com.bill.portal.scrm.customer.dto.WxCustomerMarkTagReq;
import com.bill.portal.scrm.customer.dto.WxCustomerQuery;
import com.bill.portal.scrm.customer.dto.WxCustomerRemarkReq;
import com.bill.portal.scrm.customer.dto.WxCustomerStatDTO;
import com.bill.portal.scrm.customer.entity.WxCustomer;
import com.bill.portal.scrm.customer.service.IWxCustomerService;
import com.bill.portal.scrm.customer.service.IWxCustomerSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 客户管理接口
 */
@RestController
@RequestMapping("/scrm/customer")
@Tag(name = "企微客户管理")
@AllArgsConstructor
public class WxCustomerController {

    private final IWxCustomerSyncService wxCustomerSyncService;

    private final IWxCustomerService wxCustomerService;

    @PostMapping("/sync/trigger")
    @SaCheckPermission("customer:sync")
    @Operation(summary = "手动触发客户全量同步")
    public R<?> triggerSync(@RequestParam("corpId") String corpId) {
        Long tenantId = TenantContextHolder.getTenantId();
        wxCustomerSyncService.executeFullSync(tenantId,corpId, SyncType.CUSTOMER_FULL.getCode(), TriggerType.MANUAL.getCode());
        return R.success();
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public R<IPage<WxCustomer>> page(@ParameterObject Page<WxCustomer> page, @ParameterObject @Valid WxCustomerQuery query) {
        return R.success(wxCustomerService.page(page, query));
    }

    @GetMapping("/list")
    @Operation(summary = "列表查询")
    public R<List<WxCustomer>> list(@ParameterObject @Valid WxCustomerQuery query) {
        return R.success(wxCustomerService.list(query));
    }

    @GetMapping("/statistic/summary")
    @Operation(summary = "客户统计-当日汇总", description = "实时口径：本地表即时计算（客户总数/今日新增/今日流失/累计流失）")
    public R<WxCustomerStatDTO> statisticSummary(@RequestParam("corpId") String corpId) {
        return R.success(wxCustomerService.statisticSummary(corpId));
    }

    @PostMapping("/markTag")
    @SaCheckPermission("customer:markTag")
    @Operation(summary = "编辑客户标签", description = "企微 externalcontact/mark_tag：为客户的跟进成员添加/移除企业标签，成功后刷新本地快照")
    public R<Boolean> markTag(@RequestBody @Valid WxCustomerMarkTagReq req) {
        wxCustomerSyncService.markTag(req);
        return R.success(true);
    }

    @PostMapping("/remark")
    @SaCheckPermission("customer:remark")
    @Operation(summary = "修改客户备注信息", description = "企微 externalcontact/remark：修改客户的备注/描述/备注公司/备注手机号，成功后刷新本地快照")
    public R<Boolean> remark(@RequestBody @Valid WxCustomerRemarkReq req) {
        wxCustomerSyncService.remark(req);
        return R.success(true);
    }

}
