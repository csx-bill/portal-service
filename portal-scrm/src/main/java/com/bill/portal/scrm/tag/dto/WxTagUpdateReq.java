package com.bill.portal.scrm.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "标签更新参数")
public class WxTagUpdateReq {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "标签id")
    private String tagId;

    @Schema(description = "标签名称")
    @NotBlank
    private String tagName;

    @Schema(description = "标签组次序值。order值大的排序靠前")
    private Long order;
}
