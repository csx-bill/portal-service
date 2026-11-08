package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件上传结果
 */
@Data
@Schema(description = "文件上传结果")
public class SysFileUploadDTO {

    @Schema(description = "文件名（存储侧）")
    private String fileName;

    @Schema(description = "文件访问地址")
    private String url;
}
