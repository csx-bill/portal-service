package com.bill.portal.scrm.dashboard.service;

import com.bill.portal.scrm.dashboard.dto.WxDashboardMemberRankVO;
import com.bill.portal.scrm.dashboard.dto.WxDashboardSummaryVO;
import com.bill.portal.scrm.dashboard.dto.WxDashboardTrendVO;

import java.util.List;

public interface IWxDashboardService {

    /**
     * 指标汇总（本期 + 上期，前端计算环比）
     * @param corpId 企业ID
     * @param days 统计窗口（天）
     */
    WxDashboardSummaryVO summary(String corpId, int days);

    /**
     * 按日趋势（客户/群/成员，本地实时口径）
     * @param corpId 企业ID
     * @param days 统计窗口（天）
     */
    List<WxDashboardTrendVO> trend(String corpId, int days);

    /**
     * 成员跟进客户数排行
     * @param corpId 企业ID
     * @param limit 条数
     */
    List<WxDashboardMemberRankVO> memberRank(String corpId, int limit);
}
