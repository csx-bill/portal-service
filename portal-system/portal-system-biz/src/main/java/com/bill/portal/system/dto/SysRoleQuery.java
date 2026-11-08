package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色分页查询参数。
 * 查询为单边上限（无最短长度约束）：超过列长的过滤值不可能命中数据
 */
@Data
@Schema(description = "角色分页查询参数")
public class SysRoleQuery {

    @Size(max = 50, message = "{role.name.max}")
    @Schema(description = "角色名称（模糊查询）")
    private String name;

    @Schema(description = "状态")
    private Integer status;

}
