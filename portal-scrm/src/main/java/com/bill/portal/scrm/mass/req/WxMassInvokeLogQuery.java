package com.bill.portal.scrm.mass.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WxMassInvokeLogQuery {
    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "群发任务名称")
    private String taskName;

    @Schema(description = "群发类型：single-客户，group-客户群，tag-标签客户")
    private String chatType;

    @Schema(description = "调用状态：SUCCESS-成功，FAIL-失败")
    private String status;
}
