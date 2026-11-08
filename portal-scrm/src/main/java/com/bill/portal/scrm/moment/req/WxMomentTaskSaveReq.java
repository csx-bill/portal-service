package com.bill.portal.scrm.moment.req;

import com.bill.portal.common.core.scheduler.ScheduleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WxMomentTaskSaveReq {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "群发任务名称")
    private String taskName;

    @Schema(description = "发表范围类型: ALL(全部成员) / USER(指定成员) / DEPARTMENT(指定部门) / TAG(指定客户标签)")
    private String visibleType;

    @Schema(description = "指定成员ID列表(JSON)，visible_type=USER时使用")
    private List<String> visibleUserList;

    @Schema(description = "指定部门ID列表(JSON)，visible_type=DEPARTMENT时使用")
    private List<String> visibleDeptList;

    @Schema(description = "指定标签ID列表(JSON)，visible_type=TAG时使用")
    private List<String> visibleTagList;

    @Schema(description = "任务状态：0-禁用，1-启用，2-执行中，3已完成")
    private Integer status;

    @Schema(description = "执行类型（ScheduleType 枚举，JSON 传数字编码）：0-立即执行，1-仅一次，2-每天，3-每周，4-每月，5-自定义Cron")
    private ScheduleType executeType;

    @Schema(description = "仅 execute_type=CRON 时有值")
    private String cronExpression;

    @Schema(description = "预计执行时间（立即执行时，由后端自动填充为当前时间）")
    private LocalDateTime scheduledTime;

    @Schema(description = "发送的内容")
    private List<WxMomentContentSaveReq> contentList;

}
