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
 * 系统文件分组实体
 */
@Data
@TableName(value = "sys_file_group", autoResultMap = true)
@Schema(description = "系统文件分组实体")
public class SysFileGroup extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "分组名称")
    private String name;

    @Schema(description = "上级分组ID")
    private Long parentId;

    @Schema(description = "客户端ID")
    private String clientId;

    @Schema(description = "租户ID")
    private Long tenantId;

    /** 子分组（非数据库字段） */
    @TableField(exist = false)
    @Schema(description = "子分组")
    private List<SysFileGroup> children;
}
