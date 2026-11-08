package com.bill.portal.scrm.groupchat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 客户群数据统计行（按日聚合或按群主）
 */
@Data
@Schema(description = "客户群数据统计行")
public class GroupChatStatisticDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "数据日期（按日聚合时返回）")
    private LocalDate statDate;

    @Schema(description = "群主userid（按群主聚合时返回）")
    private String owner;

    @Schema(description = "新增客户群数量")
    private Integer newChatCnt;

    @Schema(description = "截至当天客户群总数")
    private Integer chatTotal;

    @Schema(description = "新增群人数")
    private Integer newMemberCnt;

    @Schema(description = "当日退群人数")
    private Integer delMemberCnt;

    @Schema(description = "截至当天群总人数")
    private Integer memberTotal;
}
