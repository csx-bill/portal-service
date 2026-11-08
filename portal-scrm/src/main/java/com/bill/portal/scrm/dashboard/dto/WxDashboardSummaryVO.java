package com.bill.portal.scrm.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 工作台看板指标汇总（本期 + 上期，前端据此计算环比）。
 * 口径：群发送达率/朋友圈发表率按时间窗内调用记录聚合；
 * 客户总数上期值 = 当前总数 - 窗口内新增 + 窗口内流失（近似窗口起点）
 */
@Data
@Schema(description = "工作台看板指标汇总")
public class WxDashboardSummaryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "客户总数（当前）")
    private Long customerTotal;

    @Schema(description = "客户总数（窗口起点近似值）")
    private Long customerTotalPrev;

    @Schema(description = "窗口内新增客户数")
    private Long newCustomer;

    @Schema(description = "上期新增客户数")
    private Long newCustomerPrev;

    @Schema(description = "客户群总数（当前，存续群）")
    private Long groupTotal;

    @Schema(description = "客户群总数（窗口起点近似值）")
    private Long groupTotalPrev;

    @Schema(description = "群成员总数（当前，存续群）")
    private Long memberTotal;

    @Schema(description = "群成员总数（窗口起点近似值）")
    private Long memberTotalPrev;

    @Schema(description = "群发送达率（%，窗口内）")
    private Double massDeliveredRate;

    @Schema(description = "群发送达率（%，上期）")
    private Double massDeliveredRatePrev;

    @Schema(description = "朋友圈发表率（%，窗口内）")
    private Double momentPublishRate;

    @Schema(description = "朋友圈发表率（%，上期）")
    private Double momentPublishRatePrev;
}
