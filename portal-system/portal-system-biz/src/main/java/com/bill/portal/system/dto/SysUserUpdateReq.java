package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "用户更新参数")
public class SysUserUpdateReq {

    @NotNull(message = "ID不能为空")
    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码（留空则不修改）")
    private String password;

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

    @Schema(description = "所属部门ID列表")
    private List<Long> deptIds;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds;
}
