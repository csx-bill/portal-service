package com.bill.portal.scrm.userbinding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * sys_user 只读目录行（跨模块同库查询用，仅暴露绑定/展示所需字段）
 */
@Data
@Schema(description = "系统用户目录(只读)")
public class SysUserDirectoryDto {

    @Schema(description = "系统用户ID")
    private Long id;

    @Schema(description = "登录账号")
    private String username;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号")
    private String mobile;
}
