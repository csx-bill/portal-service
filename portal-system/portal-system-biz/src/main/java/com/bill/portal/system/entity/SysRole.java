package com.bill.portal.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 系统角色实体
 */
@Data
@TableName(value = "sys_role")
@Schema(description = "系统角色实体")
public class SysRole extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "状态: 0=禁用, 1=启用")
    private Integer status;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "数据权限: 1=全部, 2=自定义, 3=本级及子级, 4=本级, 5=本人")
    private Integer dataScope;

    @Schema(description = "客户端ID")
    private String clientId;

    @Schema(description = "租户ID")
    private Long tenantId;

    /** 权限菜单ID列表（非数据库字段，仅用于接收/返回） */
    @TableField(exist = false)
    @Schema(description = "权限菜单ID列表")
    private List<Long> permissions;

    /** 自定义数据权限的部门ID列表（非数据库字段，data_scope=2 时生效） */
    @TableField(exist = false)
    @Schema(description = "自定义数据权限部门ID列表")
    private List<Long> deptIds;

}
