package com.bill.portal.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统字典实体
 */
@Data
@TableName(value = "sys_dict", autoResultMap = true)
@Schema(description = "系统字典实体")
public class SysDict extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "字典类型标识")
    private String dictType;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "备注")
    private String remarks;

    @Schema(description = "是否系统内置: 0=否, 1=是")
    private String systemFlag;

    @Schema(description = "客户端ID")
    private String clientId;
}
