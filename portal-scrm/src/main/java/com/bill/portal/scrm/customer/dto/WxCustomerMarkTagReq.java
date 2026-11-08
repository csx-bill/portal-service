package com.bill.portal.scrm.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "客户标签编辑参数（企微 externalcontact/mark_tag）")
public class WxCustomerMarkTagReq {

    @Schema(description = "企业ID")
    @NotBlank(message = "企业ID不能为空")
    private String corpId;

    @Schema(description = "跟进成员userid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "跟进成员不能为空")
    private String userId;

    @Schema(description = "外部联系人userid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "外部联系人不能为空")
    private String externalUserId;

    @Schema(description = "要标记的标签ID列表")
    private List<String> addTag;

    @Schema(description = "要移除的标签ID列表")
    private List<String> removeTag;
}
