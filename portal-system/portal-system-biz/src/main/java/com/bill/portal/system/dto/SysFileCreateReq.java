package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文件新增参数。
 * 字段长度上限对齐数据库 sys_file 列定义。
 */
@Data
@Schema(description = "文件新增参数")
public class SysFileCreateReq {

    @Schema(description = "所属分组ID")
    private Long groupId;

    @NotBlank(message = "{file.name.notblank}")
    @Size(min = 1, max = 100, message = "{file.name.size}")
    @Schema(description = "文件名（存储侧）")
    private String fileName;

    @Size(max = 200, message = "{file.bucket.max}")
    @Schema(description = "存储桶名")
    private String bucketName;

    @Size(max = 200, message = "{file.dir.max}")
    @Schema(description = "存储目录")
    private String dir;

    @Size(max = 100, message = "{file.original.max}")
    @Schema(description = "原始文件名")
    private String original;

    @Size(max = 50, message = "{file.type.max}")
    @Schema(description = "文件类型")
    private String type;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;
}
