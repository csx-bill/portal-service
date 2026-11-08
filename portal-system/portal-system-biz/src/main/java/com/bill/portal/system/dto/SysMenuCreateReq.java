package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * 菜单新增参数。
 * 字段长度上限对齐数据库 sys_menu 列定义。
 */
@Data
@Schema(description = "菜单新增参数")
public class SysMenuCreateReq {

    @NotBlank(message = "{menu.name.notblank}")
    @Size(min = 2, max = 50, message = "{menu.name.size}")
    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "上级菜单ID")
    private Long parentId;

    @NotBlank(message = "{menu.menuType.notblank}")
    @Size(max = 50, message = "{menu.menuType.size}")
    @Schema(description = "菜单类型: catalog=目录, menu=菜单, button=按钮, embedded=内嵌, link=外链")
    private String menuType;

    @Size(max = 255, message = "{menu.path.size}")
    @Schema(description = "路由路径")
    private String path;

    @Size(max = 255, message = "{menu.component.size}")
    @Schema(description = "组件路径")
    private String component;

    @Size(max = 128, message = "{menu.authCode.size}")
    @Schema(description = "权限标识")
    private String authCode;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态: 0=禁用, 1=启用")
    private Integer status;

    @Schema(description = "元数据（JSON）")
    private Map<String, Object> meta;
}
