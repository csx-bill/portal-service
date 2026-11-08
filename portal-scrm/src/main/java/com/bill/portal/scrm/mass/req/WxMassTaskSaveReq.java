package com.bill.portal.scrm.mass.req;

import com.baomidou.mybatisplus.annotation.TableField;
import com.bill.portal.common.core.scheduler.ScheduleType;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WxMassTaskSaveReq {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "群发任务名称")
    private String taskName;

    @Schema(description = "群发类型：single-客户，group-客户群，tag-标签客户")
    private String chatType;

    @Schema(description = "指定发送成员userid集合")
    private List<String> senderList;

    @Schema(description = "是否允许成员二次选择客户（0-否，1-是）")
    private boolean allowSelect;

    @Schema(description = "发送目标列表")
    private List<String> targetIdList;

    @Schema(description = "客户标签过滤条件（企业微信标签ID集合）")
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private String tagFilter;

    @Schema(description = "任务状态：0-禁用，1-启用，2-执行中，3已完成")
    private Integer status;

    @Schema(description = "执行类型（ScheduleType 枚举，JSON 传数字编码）：0-立即执行，1-仅一次，2-每天，3-每周，4-每月，5-自定义Cron")
    private ScheduleType executeType;

    @Schema(description = "仅 execute_type=CRON 时有值")
    private String cronExpression;

    @Schema(description = "预计执行时间（立即执行时，由后端自动填充为当前时间）")
    private LocalDateTime scheduledTime;

    @Schema(description = "客户发送的内容")
    private List<WxMassContentSaveReq> contentList;

}
