package com.bill.portal.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户-角色关联（纯关联表，物理删除；数据权限前置：用户经角色 data_scope 确定数据范围）
 */
@Data
@TableName(value = "sys_user_role")
@Schema(description = "用户-角色关联")
public class SysUserRole implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "系统用户ID(sys_user.id)")
    private Long userId;

    @Schema(description = "角色ID(sys_role.id)")
    private Long roleId;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
