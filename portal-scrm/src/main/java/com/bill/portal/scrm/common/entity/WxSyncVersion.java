package com.bill.portal.scrm.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 同步版本记录表 (兼版本号生成器)
 * 核心设计：利用 id=1 的行记录，结合 LAST_INSERT_ID 实现基于 DB 的原子递增版本号
 */
@Data
@TableName("wx_sync_version")
@Schema(description = "同步版本记录表")
public class WxSyncVersion extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID (注意: id=1 为系统保留的版本号基准记录)")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "同步类型: FULL-全量拉取, INCREMENT-增量回调")
    private String syncType;

    @Schema(description = "执行状态: RUNNING-运行中, SUCCESS-成功, FAIL-失败, ROLLBACK-已回滚")
    private String status;

    @Schema(description = "触发类型: CRON-定时任务, MANUAL-手动触发, CALLBACK-企微回调")
    private String triggerType;

    @Schema(description = "同步开始时间")
    private LocalDateTime startTime;

    @Schema(description = "同步结束时间")
    private LocalDateTime endTime;

    @Schema(description = "版本号")
    private Long versionNo;

    @Schema(description = "备注信息 (如失败原因等)")
    private String remark;

    @Schema(description = "租户ID")
    private Long tenantId;
}
