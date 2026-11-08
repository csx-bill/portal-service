package com.bill.portal.scrm.moment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 任务维度发表统计（该任务全部批次合计，来自 wx_moment_send_log 聚合）
 */
@Data
@Schema(description = "朋友圈任务发表统计")
public class WxMomentTaskStatVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "执行批次数量")
    private Integer batchCnt;

    @Schema(description = "应发表成员数")
    private Integer totalCnt;

    @Schema(description = "已发表数（publish_status=1）")
    private Integer publishedCnt;

    @Schema(description = "未发表数（publish_status=0）")
    private Integer unpublishedCnt;
}
