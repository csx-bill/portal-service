package com.bill.portal.scrm.mass.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 成员维度发送统计（wx_mass_target_log 按 user_id 聚合）
 */
@Data
@Schema(description = "群发成员发送统计")
public class WxMassUserStatVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "调用记录ID（按批次+成员聚合时携带）")
    private Long invokeId;

    @Schema(description = "企微成员userid")
    private String userId;

    @Schema(description = "应发送总数")
    private Integer totalCnt;

    @Schema(description = "已送达数（status=1）")
    private Integer deliveredCnt;

    @Schema(description = "未发送数（status=0，成员未确认）")
    private Integer unsentCnt;

    @Schema(description = "失败数-客户非好友（status=2）")
    private Integer failFriendCnt;

    @Schema(description = "失败数-已收到其他群发（status=3）")
    private Integer failOtherCnt;
}
