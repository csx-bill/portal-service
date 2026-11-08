package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "租户分页查询参数")
public class SysTenantQuery {

    @Schema(description = "租户名称（模糊查询）")
    private String name;

    @Schema(description = "状态")
    private Integer status;

}
