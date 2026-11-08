package com.bill.portal.scrm.mass.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企微群发目标记录实体
 */
@Data
@TableName(value = "wx_mass_target_log", autoResultMap = true)
@Schema(description = "企微群发目标记录实体")
public class WxMassTargetLog extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "调用记录ID")
    private Long invokeId;

    @Schema(description = "群发类型：single-客户，group-客户群，tag-标签客户")
    private String chatType;

    @Schema(description = "客户external_userid 或 客户群chat_id")
    private String targetId;

    @Schema(description = "企微账号ID")
    private String userId;

    @Schema(description = "发送状态：0-未发送 1-已发送 2-因客户不是好友导致发送失败 3-因客户已经收到其他群发消息导致发送失败")
    private Integer status;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "租户ID")
    private Long tenantId;
}
