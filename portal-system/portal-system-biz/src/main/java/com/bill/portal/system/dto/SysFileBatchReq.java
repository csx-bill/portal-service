package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 文件批量操作参数（批量删除 / 批量移动）
 */
@Data
@Schema(description = "文件批量操作参数")
public class SysFileBatchReq {

    @NotEmpty(message = "{file.ids.notempty}")
    @Schema(description = "文件ID列表")
    private List<Long> ids;

    @Schema(description = "目标分组ID（批量移动时使用，null 表示移到未分组）")
    private Long groupId;
}
