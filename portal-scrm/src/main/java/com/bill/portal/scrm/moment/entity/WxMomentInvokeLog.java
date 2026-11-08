package com.bill.portal.scrm.moment.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 企微朋友圈调用记录实体
 */
@Data
@TableName(value = "wx_moment_invoke_log", autoResultMap = true)
@Schema(description = "企微朋友圈调用记录实体")
public class WxMomentInvokeLog extends BaseEntity implements Serializable {
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

    @Schema(description = "发表范围: all(全部成员) / user(指定成员) / department(指定部门) / tag(指定客户标签)")
    private String visibleType;

    @Schema(description = "指定成员ID列表(JSON)，visible_type=USER时使用")
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> visibleUserList;

    @Schema(description = "指定部门ID列表(JSON)，visible_type=DEPARTMENT时使用")
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> visibleDeptList;

    @Schema(description = "指定标签ID列表(JSON)，visible_type=TAG时使用")
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> visibleTagList;

    @Schema(description = "调用状态：SUCCESS-成功，FAIL-失败")
    private String status;

    @Schema(description = "异步任务id")
    private String jobid;

    @Schema(description = "触发类型")
    private String triggerType;

    @Schema(description = "错误信息")
    private String errmsg;

    @TableField(typeHandler = Jackson3TypeHandler.class)
    @Schema(description = "原始完整数据快照")
    private String extraData;

    @Schema(description = "朋友圈id")
    private String momentId;

    @Schema(description = "朋友圈状态:1表示开始创建任务，2表示正在创建任务中，3表示创建任务已完成")
    private Integer momentStatus;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "批次号（同一批次触发的任务共享）")
    private String batchNo;
}
