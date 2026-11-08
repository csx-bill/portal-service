package com.bill.portal.scrm.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "企微客户统计-当日汇总")
public class WxCustomerStatDTO {

    @Schema(description = "客户总数")
    private Long total;

    @Schema(description = "今日新增（按添加时间）")
    private Long todayNew;

    @Schema(description = "今日流失")
    private Long todayLost;

    @Schema(description = "累计流失")
    private Long lostTotal;
}
