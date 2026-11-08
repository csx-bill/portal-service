package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserDTO {
    @Schema(description = "用户名")
    private String username;

    @Schema(description = "手机号")
    private String mobile;
}
