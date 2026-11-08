package com.bill.portal.scrm.moment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 单任务发表统计（GET /scrm/moment/statistic/task/{taskId} 返回体）：
 * 全部批次合计 + 各批次明细（含成员维度）
 */
@Data
@Schema(description = "朋友圈任务批次发表统计")
public class WxMomentTaskBatchStatResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "全部批次合计")
    private WxMomentTaskStatVO summary;

    @Schema(description = "各批次统计（按调用记录维度）")
    private List<WxMomentBatchStatVO> batches;
}
