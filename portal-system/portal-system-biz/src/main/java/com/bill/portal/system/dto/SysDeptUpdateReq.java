package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 部门更新参数。
 * 字段长度上限对齐数据库 sys_dept 列定义（name varchar(50)）。
 */
@Data
@Schema(description = "部门更新参数")
public class SysDeptUpdateReq {

    @NotNull(message = "{validation.id.notnull}")
    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @NotBlank(message = "{dept.name.notblank}")
    @Size(max = 50, message = "{dept.name.size}")
    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "上级部门ID（空为根部门）")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sortOrder;
}
