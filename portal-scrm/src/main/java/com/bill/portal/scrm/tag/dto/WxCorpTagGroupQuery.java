package com.bill.portal.scrm.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "企微客户标签组查询参数")
public class WxCorpTagGroupQuery {

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "标签组名称")
    private String groupName;
}
