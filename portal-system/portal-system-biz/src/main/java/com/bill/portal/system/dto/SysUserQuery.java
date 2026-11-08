package com.bill.portal.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户分页查询参数")
public class SysUserQuery {

    @Schema(description = "关键字（用户名/姓名/手机号模糊查询）")
    private String keyword;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "部门ID（含其子部门，过滤该部门下用户）")
    private Long deptId;
}
