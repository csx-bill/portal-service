package com.bill.portal.scrm.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "企微客户分页查询参数")
public class WxCustomerQuery {
    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "外部联系人的名称")
    private String name;

    @Schema(description = "企业成员userid")
    private String userId;

    @Schema(description = "是否流失:0-正常,1-流失（不传查全部）")
    private Integer lostFlag;

    @Schema(description = "部门ID（通讯录树过滤用，命中部门及子部门下的成员）")
    private Long deptId;

    @Schema(description = "客户标签ID（通讯录客户标签，命中 extra_data 快照里的任一标签）")
    private String tagId;
}
