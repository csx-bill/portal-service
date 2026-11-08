package com.bill.portal.scrm.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 工作台看板成员排行行（按跟进客户数）
 */
@Data
@Schema(description = "工作台成员客户数排行")
public class WxDashboardMemberRankVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "企微成员userid")
    private String userId;

    @Schema(description = "成员姓名（wx_user.name，未匹配到则为 userid）")
    private String name;

    @Schema(description = "跟进客户数")
    private Long customerCnt;
}
