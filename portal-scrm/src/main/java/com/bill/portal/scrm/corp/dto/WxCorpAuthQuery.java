package com.bill.portal.scrm.corp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "企业分页查询参数")
public class WxCorpAuthQuery {
    @Schema(description = "企业ID (企微唯一标识)")
    private String corpId;

    @Schema(description = "企业名称")
    private String corpName;
}
