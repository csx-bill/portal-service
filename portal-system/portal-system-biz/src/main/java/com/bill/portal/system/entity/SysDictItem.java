package com.bill.portal.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统字典项实体
 */
@Data
@TableName(value = "sys_dict_item", autoResultMap = true)
@Schema(description = "系统字典项实体")
public class SysDictItem extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "所属字典ID")
    private Long dictId;

    @Schema(description = "字典项值")
    private String itemValue;

    @Schema(description = "字典项标签")
    private String label;

    @Schema(description = "字典类型标识（冗余自 sys_dict）")
    private String dictType;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remarks;

    @Schema(description = "客户端ID")
    private String clientId;
}
