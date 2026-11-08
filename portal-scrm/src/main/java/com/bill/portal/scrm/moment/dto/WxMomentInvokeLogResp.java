package com.bill.portal.scrm.moment.dto;

import com.bill.portal.scrm.moment.req.WxMomentContentSaveReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 企微朋友圈调用记录详情（GET /scrm/moment/invokeLog/{id} 返回体）。
 * <p>
 * 刻意不复用 {@link com.bill.portal.scrm.moment.req.WxMomentTaskSaveReq}：
 * 任务状态（0-3 Integer）与调用状态（SUCCESS/FAIL String）语义不同，
 * 实体直接拷贝会类型转换失败，这里让两种状态各归其位。
 */
@Data
@Schema(description = "企微朋友圈调用记录详情")
public class WxMomentInvokeLogResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "调用记录ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "群发任务名称")
    private String taskName;

    @Schema(description = "发表范围: all(全部成员) / user(指定成员) / department(指定部门) / tag(指定客户标签)")
    private String visibleType;

    @Schema(description = "指定成员ID列表（visible_type=user 时使用）")
    private List<String> visibleUserList;

    @Schema(description = "指定部门ID列表（visible_type=department 时使用）")
    private List<String> visibleDeptList;

    @Schema(description = "指定标签ID列表（visible_type=tag 时使用）")
    private List<String> visibleTagList;

    @Schema(description = "调用状态：SUCCESS-成功，FAIL-失败")
    private String status;

    @Schema(description = "企微异步任务id")
    private String jobid;

    @Schema(description = "朋友圈任务id")
    private String momentId;

    @Schema(description = "朋友圈任务状态（企微 get_moment_task 返回状态，3 为终态并回填 momentId）")
    private Integer momentStatus;

    @Schema(description = "触发类型")
    private String triggerType;

    @Schema(description = "错误信息")
    private String errmsg;

    @Schema(description = "调用时间")
    private LocalDateTime createTime;

    @Schema(description = "该次调用的内容快照")
    private List<WxMomentContentSaveReq> contentList;
}
