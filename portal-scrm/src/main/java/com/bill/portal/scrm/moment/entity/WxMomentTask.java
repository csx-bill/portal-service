package com.bill.portal.scrm.moment.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.bill.portal.common.core.model.BaseEntity;
import com.bill.portal.common.core.scheduler.ScheduleType;
import com.bill.portal.common.core.scheduler.TaskInstance;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 企微朋友圈任务实体
 */
@Data
@TableName(value = "wx_moment_task", autoResultMap = true)
@Schema(description = "企微朋友圈任务实体")
public class WxMomentTask extends BaseEntity implements TaskInstance {
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

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


    @Schema(description = "任务状态：0-禁用，1-启用，2-执行中，3已完成")
    private Integer status;

    @Schema(description = "执行类型（直接使用 ScheduleType 枚举，数据库存枚举名）：IMMEDIATE-立即执行，ONCE-仅一次，DAILY-每天，WEEKLY-每周，MONTHLY-每月，CRON-自定义Cron")
    private ScheduleType executeType;

    @Schema(description = "仅 execute_type=CRON 时有值")
    private String cronExpression;

    @Schema(description = "预计执行时间（立即执行时，由后端自动填充为当前时间）")
    private LocalDateTime scheduledTime;

    @Schema(description = "租户ID")
    private Long tenantId;

    // ────── TaskInstance 实现 ──────
    @Override
    public Long getTaskId() {
        return id;
    }

    @Override
    public Integer getStatus() {
        return status;
    }

    @Override
    public ScheduleType getScheduleType() {
        // 字段即枚举类型；历史数据 execute_type 为 NULL 时兜底为仅一次（与旧逻辑一致）
        return executeType != null ? executeType : ScheduleType.ONCE;
    }

    @Override
    public String getCronExpression() {
        return cronExpression;
    }

    @Override
    public LocalDateTime getBaseTime() {
        return scheduledTime;
    }
}
