package com.bill.portal.scrm.moment.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WxMomentTaskQueryReq {
    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "群发任务名称")
    private String taskName;

    @Schema(description = "发表范围: all(全部成员) / user(指定成员) / department(指定部门) / tag(指定客户标签)")
    private String visibleType;

    @Schema(description = "任务状态：1-启用，0-停用")
    private Integer status;

    @Schema(description = "可见成员userid（仅匹配 visible_type=USER 的任务，通讯录树过滤用）")
    private String sender;

    @Schema(description = "可见部门ID（仅匹配 visible_type=DEPARTMENT 的任务，通讯录树过滤用）")
    private Long deptId;
}
