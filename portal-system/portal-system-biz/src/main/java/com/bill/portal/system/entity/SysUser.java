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
 * 系统用户实体
 */
@Data
@TableName(value = "sys_user")
@Schema(description = "系统用户实体")
public class SysUser extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "随机盐")
    private String salt;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "性别：0-未知 1-男性 2-女性")
    private Integer gender;

    @Schema(description = "状态: 0=禁用, 1=启用")
    private Integer status;

    /** 所属部门ID列表（非数据库字段，随详情/分页返回） */
    @TableField(exist = false)
    @Schema(description = "所属部门ID列表")
    private List<Long> deptIds;

    /** 所属部门名称列表（非数据库字段，随详情/分页返回） */
    @TableField(exist = false)
    @Schema(description = "所属部门名称列表")
    private List<String> deptNames;

    /** 角色ID列表（非数据库字段，随详情返回；数据权限按角色 data_scope 确定） */
    @TableField(exist = false)
    @Schema(description = "角色ID列表")
    private List<Long> roleIds;

}
