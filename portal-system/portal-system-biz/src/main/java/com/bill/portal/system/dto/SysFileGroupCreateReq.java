package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文件分组新增参数。
 * 字段长度上限对齐数据库 sys_file_group 列定义（name varchar(32)）。
 */
@Data
@Schema(description = "文件分组新增参数")
public class SysFileGroupCreateReq {

    @NotBlank(message = "{fileGroup.name.notblank}")
    @Size(min = 1, max = 32, message = "{fileGroup.name.size}")
    @Schema(description = "分组名称")
    private String name;

    @Schema(description = "上级分组ID")
    private Long parentId;
}
