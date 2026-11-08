package com.bill.portal.scrm.mass.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WxMassTaskQuery {
    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "群发任务名称")
    private String taskName;

    @Schema(description = "群发类型：single-客户，group-客户群，tag-标签客户")
    private String chatType;

    @Schema(description = "任务状态：1-启用，0-停用")
    private Integer status;

    @Schema(description = "发送成员userid（按 sender_list 过滤，通讯录树过滤用）")
    private String sender;

    @Schema(description = "部门ID（通讯录树过滤用，按发送成员命中部门及子部门下的成员）")
    private Long deptId;
}
