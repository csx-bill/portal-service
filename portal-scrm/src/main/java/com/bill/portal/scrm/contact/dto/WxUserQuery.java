package com.bill.portal.scrm.contact.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "企微成员分页查询参数")
public class WxUserQuery {
    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "姓名（模糊匹配）")
    private String name;

    @Schema(description = "激活状态: 1=已激活，2=已禁用，4=未激活，5=退出企业")
    private Integer status;

    @Schema(description = "部门ID：成员的 departIds 命中该部门即返回")
    private Long deptId;
}
