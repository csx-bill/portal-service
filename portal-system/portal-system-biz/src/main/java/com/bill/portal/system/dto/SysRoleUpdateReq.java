package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 角色更新参数。
 * 字段长度上限对齐数据库 sys_role 列定义（name varchar(50)、remark varchar(255)）。
 */
@Data
@Schema(description = "角色更新参数")
public class SysRoleUpdateReq {

    @NotNull(message = "{validation.id.notnull}")
    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Size(min = 2, max = 50, message = "{role.name.size}")
    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "状态: 0=禁用, 1=启用")
    private Integer status;

    @Size(max = 255, message = "{role.remark.size}")
    @Schema(description = "备注信息")
    private String remark;

    @NotNull(message = "{role.dataScope.notnull}")
    @Schema(description = "数据权限: 1=全部, 2=自定义, 3=本级及子级, 4=本级, 5=本人", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer dataScope;

    @Schema(description = "权限菜单ID列表")
    private List<Long> permissions;

    @Schema(description = "自定义数据权限部门ID列表（dataScope=2 时生效）")
    private List<Long> deptIds;
}
