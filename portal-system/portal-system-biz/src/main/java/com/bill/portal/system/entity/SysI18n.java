package com.bill.portal.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统国际化实体
 */
@Data
@TableName(value = "sys_i18n", autoResultMap = true)
@Schema(description = "系统国际化实体")
public class SysI18n extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "name")
    private String name;

    @Schema(description = "中文")
    private String zh;

    @Schema(description = "英文")
    private String en;

    @Schema(description = "客户端ID")
    private String clientId;
}
