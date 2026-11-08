package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 国际化分页查询参数（单边上限，理由同 SysRoleQuery）
 */
@Data
@Schema(description = "国际化分页查询参数")
public class SysI18nQuery {

    @Size(max = 50, message = "{i18n.name.max}")
    @Schema(description = "name")
    private String name;
}
