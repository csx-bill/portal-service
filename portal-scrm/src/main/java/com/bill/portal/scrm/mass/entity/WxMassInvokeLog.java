package com.bill.portal.scrm.mass.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 企微群发调用记录实体
 */
@Data
@TableName(value = "wx_mass_invoke_log", autoResultMap = true)
@Schema(description = "企微群发调用记录实体")
public class WxMassInvokeLog extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "群发任务名称")
    private String taskName;

    @Schema(description = "群发类型：single-客户，group-客户群，tag-标签客户")
    private String chatType;

    @Schema(description = "指定发送成员userid")
    private String sender;

    @Schema(description = "是否允许成员二次选择客户（0-否，1-是）")
    private boolean allowSelect;

    @Schema(description = "客户标签过滤条件（企业微信标签ID集合）")
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private String tagFilter;

    @Schema(description = "调用状态：SUCCESS-成功，FAIL-失败")
    private String status;

    @Schema(description = "群发消息ID")
    private String msgid;

    @Schema(description = "触发类型")
    private String triggerType;

    @Schema(description = "错误信息")
    private String errmsg;

    @TableField(typeHandler = Jackson3TypeHandler.class)
    @Schema(description = "原始完整数据快照")
    private String extraData;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "批次号（同一批次触发的任务共享）")
    private String batchNo;
}
