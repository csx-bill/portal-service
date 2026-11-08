package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 国际化更新参数。
 * 字段长度上限对齐数据库 sys_i18n 列定义（name varchar(50)、zh/en varchar(255)）。
 */
@Data
@Schema(description = "国际化更新参数")
public class SysI18nUpdateReq {

    @NotNull(message = "{validation.id.notnull}")
    @Schema(description = "主键ID")
    private Long id;

    @Size(min = 2, max = 50, message = "{i18n.name.size}")
    @Schema(description = "name")
    private String name;

    @Size(max = 255, message = "{i18n.zh.size}")
    @Schema(description = "中文")
    private String zh;

    @Size(max = 255, message = "{i18n.en.size}")
    @Schema(description = "英文")
    private String en;
}
