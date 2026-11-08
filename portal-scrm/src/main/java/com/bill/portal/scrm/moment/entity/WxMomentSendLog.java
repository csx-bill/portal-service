package com.bill.portal.scrm.moment.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 朋友圈成员发表记录实体（wx_moment_send_log）。
 * 任务创建完成后由结果同步 Job 调企微 get_moment_task 落库，
 * 每次调用（invoke_id）× 每个可见成员（user_id）一条。
 */
@Data
@TableName(value = "wx_moment_send_log")
@Schema(description = "朋友圈成员发表记录实体")
public class WxMomentSendLog extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "调用记录ID")
    private Long invokeId;

    @Schema(description = "企微成员userid")
    private String userId;

    @Schema(description = "发表状态：0-未发表，1-已发表")
    private Integer publishStatus;

    @Schema(description = "租户ID")
    private Long tenantId;
}
