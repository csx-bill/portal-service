package com.bill.portal.scrm.moment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 单批次（一次调用 invoke_log）发表统计，来自 wx_moment_send_log 按 invoke_id 聚合
 */
@Data
@Schema(description = "朋友圈单批次发表统计")
public class WxMomentBatchStatVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "调用记录ID")
    private Long invokeId;

    @Schema(description = "应发表成员数")
    private Integer totalCnt;

    @Schema(description = "已发表数（publish_status=1）")
    private Integer publishedCnt;

    @Schema(description = "未发表数（publish_status=0）")
    private Integer unpublishedCnt;

    @Schema(description = "成员发表明细（单任务统计接口返回）")
    private List<WxMomentUserStatVO> members;
}
