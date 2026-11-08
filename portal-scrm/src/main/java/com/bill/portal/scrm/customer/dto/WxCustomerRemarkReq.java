package com.bill.portal.scrm.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 修改客户备注信息参数（企微 externalcontact/remark，文档 92694）。
 * remark / description / remarkCompany / remarkMobiles 不可同时为空（服务端校验）。
 */
@Data
@Schema(description = "修改客户备注信息参数")
public class WxCustomerRemarkReq {

    @Schema(description = "企业ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "企业ID不能为空")
    private String corpId;

    @Schema(description = "企业成员userid（客户的跟进成员）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "跟进成员不能为空")
    private String userId;

    @Schema(description = "外部联系人userid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "外部联系人不能为空")
    private String externalUserId;

    @Schema(description = "备注信息（最多20个字符）")
    @Size(max = 20, message = "备注最多 {max} 个字符")
    private String remark;

    @Schema(description = "描述信息（最多150个字符）")
    @Size(max = 150, message = "描述最多 {max} 个字符")
    private String description;

    @Schema(description = "备注的所属公司名称（最多20个字符，仅微信用户有效）")
    @Size(max = 20, message = "备注公司最多 {max} 个字符")
    private String remarkCompany;

    @Schema(description = "备注手机号（覆盖式更新，传空数组即清除）")
    private List<String> remarkMobiles;
}
