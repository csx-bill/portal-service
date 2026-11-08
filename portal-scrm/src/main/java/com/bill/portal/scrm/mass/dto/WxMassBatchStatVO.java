package com.bill.portal.scrm.mass.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 单批次（一次调用 invoke_log）送达统计，来自 wx_mass_target_log 按 invoke_id 聚合
 */
@Data
@Schema(description = "群发单批次送达统计")
public class WxMassBatchStatVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "调用记录ID")
    private Long invokeId;

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

    @Schema(description = "成员发送明细（单任务统计接口返回）")
    private List<WxMassUserStatVO> members;
}
