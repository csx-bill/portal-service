package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文件分页查询参数（单边上限，理由同 SysRoleQuery）
 */
@Data
@Schema(description = "文件分页查询参数")
public class SysFileQuery {

    @Schema(description = "所属分组ID")
    private Long groupId;

    @Schema(description = "仅查询未分组文件")
    private Boolean ungrouped;

    @Size(max = 20, message = "{file.category.max}")
    @Schema(description = "类型分类: image=图片, video=视频, file=其他文件")
    private String typeCategory;

    @Size(max = 100, message = "{file.name.max}")
    @Schema(description = "文件名（模糊查询）")
    private String fileName;
}
