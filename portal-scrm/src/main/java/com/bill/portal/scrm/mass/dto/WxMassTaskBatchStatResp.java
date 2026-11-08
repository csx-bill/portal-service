package com.bill.portal.scrm.mass.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 单任务送达统计（GET /scrm/mass/statistic/task/{taskId} 返回体）：
 * 全部批次合计 + 各批次明细（含成员维度）
 */
@Data
@Schema(description = "群发任务批次送达统计")
public class WxMassTaskBatchStatResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "全部批次合计")
    private WxMassTaskStatVO summary;

    @Schema(description = "各批次统计（按调用记录维度）")
    private List<WxMassBatchStatVO> batches;
}
