package com.bill.portal.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 系统菜单实体
 */
@Data
@TableName(value = "sys_menu", autoResultMap = true)
@Schema(description = "系统菜单实体")
public class SysMenu extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "上级菜单ID")
    private Long parentId;

    @Schema(description = "菜单类型: catalog=目录, menu=菜单, button=按钮, embedded=内嵌, link=外链")
    private String menuType;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "权限标识")
    private String authCode;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态: 0=禁用, 1=启用")
    private Integer status;

    @Schema(description = "客户端ID")
    private String clientId;

    /**
     * 元数据 JSON
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    @Schema(description = "元数据（JSON）")
    private Map<String, Object> meta;

    /** 子菜单（非数据库字段） */
    @TableField(exist = false)
    private List<SysMenu> children;

}
