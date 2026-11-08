package com.bill.portal.scrm.mass.dto;

import com.bill.portal.scrm.mass.req.WxMassContentSaveReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 群发调用记录详情（GET /scrm/mass/invokeLog/{id} 返回体）。
 * <p>
 * 刻意不复用 {@link com.bill.portal.scrm.mass.req.WxMassTaskSaveReq}：
 * 任务状态（0-3 Integer）与调用状态（SUCCESS/FAIL String）语义不同，
 * 实体直接拷贝会类型转换失败，这里让两种状态各归其位。
 */
@Data
@Schema(description = "群发调用记录详情")
public class WxMassInvokeLogResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "调用记录ID")
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

    @Schema(description = "调用状态：SUCCESS-成功，FAIL-失败")
    private String status;

    @Schema(description = "群发消息ID")
    private String msgid;

    @Schema(description = "触发类型")
    private String triggerType;

    @Schema(description = "错误信息")
    private String errmsg;

    @Schema(description = "客户标签过滤条件（企业微信标签ID集合）")
    private String tagFilter;

    @Schema(description = "调用时间")
    private LocalDateTime createTime;

    @Schema(description = "该次调用的发送目标列表")
    private List<String> targetIdList;

    @Schema(description = "该次调用的内容快照")
    private List<WxMassContentSaveReq> contentList;
}
