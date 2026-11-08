package com.bill.portal.scrm.moment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 成员维度发表统计（wx_moment_send_log 按 user_id 聚合）
 */
@Data
@Schema(description = "朋友圈成员发表统计")
public class WxMomentUserStatVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "调用记录ID（按批次+成员聚合时携带）")
    private Long invokeId;

    @Schema(description = "企微成员userid")
    private String userId;

    @Schema(description = "应发表成员数（恒为1，单成员单批次一条记录）")
    private Integer totalCnt;

    @Schema(description = "已发表数（publish_status=1）")
    private Integer publishedCnt;

    @Schema(description = "未发表数（publish_status=0）")
    private Integer unpublishedCnt;
}
