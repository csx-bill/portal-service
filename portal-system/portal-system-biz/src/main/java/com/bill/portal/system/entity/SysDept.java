package com.bill.portal.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 系统部门实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_dept")
@Schema(description = "系统部门实体")
public class SysDept extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "上级部门ID（空为根部门）")
    private Long parentId;

    @Schema(description = "租户ID")
    private Long tenantId;

    /** 子部门（非数据库字段，树接口组装） */
    @TableField(exist = false)
    @Schema(description = "子部门列表")
    private List<SysDept> children;

}
