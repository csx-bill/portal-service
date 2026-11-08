package com.bill.portal.scrm.moment.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WxMomentInvokeLogQueryReq {
    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "群发任务名称")
    private String taskName;

    @Schema(description = "发表范围类型: ALL(全部成员) / USER(指定成员) / DEPARTMENT(指定部门) / TAG(指定客户标签)")
    private String visibleType;

    @Schema(description = "调用状态：SUCCESS-成功，FAIL-失败")
    private String status;
}
