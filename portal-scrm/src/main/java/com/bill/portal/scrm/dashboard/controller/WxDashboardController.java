package com.bill.portal.scrm.dashboard.controller;

import com.bill.portal.common.core.model.R;
import com.bill.portal.scrm.dashboard.dto.WxDashboardMemberRankVO;
import com.bill.portal.scrm.dashboard.dto.WxDashboardSummaryVO;
import com.bill.portal.scrm.dashboard.dto.WxDashboardTrendVO;
import com.bill.portal.scrm.dashboard.service.IWxDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 工作台看板（只读聚合，全部为本地表实时口径）
 */
@Tag(name = "工作台看板")
@RestController
@RequestMapping("/scrm/dashboard")
@AllArgsConstructor
public class WxDashboardController {

    private final IWxDashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "指标汇总" , description = "客户/群/成员总数与新增、群发送达率、朋友圈发表率，含上期值供前端计算环比" )
    public R<WxDashboardSummaryVO> summary(@RequestParam("corpId") String corpId,
            @RequestParam(value = "days", defaultValue = "7") int days) {
        return R.success(dashboardService.summary(corpId, days));
    }

    @GetMapping("/trend")
    @Operation(summary = "按日趋势" , description = "新增客户/新增群/新增群人数/退群人数，按本地时间戳口径" )
    public R<List<WxDashboardTrendVO>> trend(@RequestParam("corpId") String corpId,
            @RequestParam(value = "days", defaultValue = "7") int days) {
        return R.success(dashboardService.trend(corpId, days));
    }

    @GetMapping("/rank/member")
    @Operation(summary = "成员跟进客户数排行" , description = "按跟进成员聚合客户数量" )
    public R<List<WxDashboardMemberRankVO>> memberRank(@RequestParam("corpId") String corpId,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return R.success(dashboardService.memberRank(corpId, limit));
    }

}
