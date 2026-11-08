package com.bill.portal.scrm.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "标签组更新参数")
public class WxTagGroupUpdateReq {

    @Schema(description = "主键ID")
    private Long id;

    @NotBlank
    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "标签组id")
    private String groupId;

    @Schema(description = "标签组名称")
    @NotBlank
    private String groupName;

    @Schema(description = "标签组次序值。order值大的排序靠前")
    private Long order;

    @Schema(description = "标签列表")
    private List<WxTagUpdateReq> tagList;
}
