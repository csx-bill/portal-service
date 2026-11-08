package com.bill.portal.scrm.mass.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 任务维度送达统计（该任务全部批次合计，来自 wx_mass_target_log 聚合）。
 * <p>
 * 口径与调用记录抽屉一致：未发送（status=0，成员未确认）不计入失败，
 * 失败 = 客户非好友（status=2）+ 已收到其他群发（status=3）。
 */
@Data
@Schema(description = "群发任务送达统计")
public class WxMassTaskStatVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "执行批次数量")
    private Integer batchCnt;

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
