package com.bill.portal.scrm.inheritance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 待分配的离职成员客户列表查询参数
 */
@Data
@Schema(description = "待分配的离职成员客户列表查询参数")
public class InheritUnassignedQueryDTO {

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "分页游标（来自上次返回的 nextCursor，查下一页时传入）")
    private String cursor;

    @Schema(description = "每页记录数，默认1000，最大1000")
    private Integer pageSize;
}
