package com.bill.portal.common.core.security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserInfo {
    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "随机盐")
    private String salt;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "性别：0-未知 1-男性 2-女性")
    private Integer gender;

    @Schema(description = "状态: 0=禁用, 1=启用")
    private Integer status;

    @Schema(description = "用户角色")
    private List<Long> roles = new ArrayList<>();

    @Schema(description = "权限标识集合")
    private List<String> permissions = new ArrayList<>();
}
