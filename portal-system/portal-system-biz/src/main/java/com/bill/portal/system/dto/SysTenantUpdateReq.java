package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "租户更新参数")
public class SysTenantUpdateReq {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "租户名称")
    private String name;

    @Schema(description = "租户编码")
    private String code;

    @Schema(description = "状态: 0=禁用, 1=启用")
    private Integer status;

    @Schema(description = "备注信息")
    private String remark;
}
