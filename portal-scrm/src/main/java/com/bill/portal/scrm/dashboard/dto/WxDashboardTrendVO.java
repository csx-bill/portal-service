package com.bill.portal.scrm.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 工作台看板按日趋势行（本地实时口径：客户按 follow_create_time、
 * 群按 COALESCE(group_create_time, create_time)、
 * 群成员按 join_time、退群按成员变动流水 change_time）
 */
@Data
@Schema(description = "工作台看板按日趋势行")
public class WxDashboardTrendVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "数据日期（yyyy-MM-dd）")
    private String date;

    @Schema(description = "新增客户数")
    private Long newCustomer;

    @Schema(description = "新增群数")
    private Long newGroup;

    @Schema(description = "新增群人数")
    private Long newMember;

    @Schema(description = "退群人数")
    private Long quitMember;
}
