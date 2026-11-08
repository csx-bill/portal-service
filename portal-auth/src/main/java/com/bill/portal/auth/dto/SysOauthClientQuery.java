package com.bill.portal.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "终端分页查询参数")
public class SysOauthClientQuery {

    @Schema(description = "客户端ID")
    private String clientId;

    @Schema(description = "状态: 0=禁用, 1=启用")
    private Integer status;
}
