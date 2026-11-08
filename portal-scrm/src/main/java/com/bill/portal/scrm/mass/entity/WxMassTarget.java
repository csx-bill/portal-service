package com.bill.portal.scrm.mass.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 企微群发目标实体
 */
@Data
@TableName(value = "wx_mass_target", autoResultMap = true)
@Schema(description = "企微群发目标实体")
public class WxMassTarget extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "群发类型：single-客户，group-客户群，tag-标签客户")
    private String chatType;

    @Schema(description = "客户external_userid 或 客户群chat_id")
    private String targetId;

    @Schema(description = "租户ID")
    private Long tenantId;
}
