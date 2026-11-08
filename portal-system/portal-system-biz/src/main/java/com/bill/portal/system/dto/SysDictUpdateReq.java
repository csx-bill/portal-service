package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典更新参数。
 * 字段长度上限对齐数据库 sys_dict 列定义。
 */
@Data
@Schema(description = "字典更新参数")
public class SysDictUpdateReq {

    @NotNull(message = "{validation.id.notnull}")
    @Schema(description = "主键ID")
    private Long id;

    @Size(min = 2, max = 100, message = "{dict.type.size}")
    @Schema(description = "字典类型标识")
    private String dictType;

    @NotBlank(message = "{dict.description.notblank}")
    @Size(min = 1, max = 100, message = "{dict.description.size}")
    @Schema(description = "字典名称（描述）")
    private String description;

    @Size(max = 255, message = "{dict.remarks.max}")
    @Schema(description = "备注")
    private String remarks;

    @Schema(description = "是否系统内置: 0=否, 1=是")
    private String systemFlag;
}
