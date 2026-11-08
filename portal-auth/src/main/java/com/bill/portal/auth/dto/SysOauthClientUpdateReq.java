package com.bill.portal.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "终端更新参数")
public class SysOauthClientUpdateReq {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "客户端ID")
    private String clientId;

    @Schema(description = "客户端秘钥")
    private String clientSecret;

    @Schema(description = "授权范围")
    public String contractScopes;

    @Schema(description = "允许授权的所有URL")
    public String allowRedirectUris;

    @Schema(description = "授权类型")
    public String allowGrantTypes;

    @Schema(description = "状态: 0=禁用, 1=启用")
    private Integer status;
}
