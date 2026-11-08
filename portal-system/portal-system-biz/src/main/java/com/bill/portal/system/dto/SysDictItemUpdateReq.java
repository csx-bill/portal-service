package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典项更新参数。
 * 字段长度上限对齐数据库 sys_dict_item 列定义。
 */
@Data
@Schema(description = "字典项更新参数")
public class SysDictItemUpdateReq {

    @NotNull(message = "{validation.id.notnull}")
    @Schema(description = "主键ID")
    private Long id;

    @NotNull(message = "{dictItem.dictId.notnull}")
    @Schema(description = "所属字典ID")
    private Long dictId;

    @Size(min = 1, max = 100, message = "{dictItem.label.size}")
    @Schema(description = "字典项标签")
    private String label;

    @Size(min = 1, max = 100, message = "{dictItem.value.size}")
    @Schema(description = "字典项值")
    private String itemValue;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Size(max = 100, message = "{dictItem.description.max}")
    @Schema(description = "描述")
    private String description;

    @Size(max = 255, message = "{dictItem.remarks.max}")
    @Schema(description = "备注")
    private String remarks;
}
