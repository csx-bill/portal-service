package com.bill.portal.scrm.userbinding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "绑定/换绑参数")
public class WxUserBindingSaveReq {

    @NotNull
    @Schema(description = "系统用户ID")
    private Long sysUserId;

    @NotBlank
    @Schema(description = "企业ID（成员所属企业，一个租户可授权多家企业）")
    private String corpId;

    @NotBlank
    @Schema(description = "企微成员ID(wx_user.userid)")
    private String wxUserid;
}
