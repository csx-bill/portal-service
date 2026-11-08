package com.bill.portal.scrm.userbinding.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "系统用户-企微成员绑定")
public class WxUserBindingVo {

    @Schema(description = "绑定记录ID")
    private Long id;

    @Schema(description = "系统用户ID")
    private Long sysUserId;

    @Schema(description = "系统用户姓名")
    private String sysUserName;

    @Schema(description = "系统用户登录账号")
    private String sysUserUsername;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "企微成员ID")
    private String wxUserid;

    @Schema(description = "企微成员姓名")
    private String wxUserName;

    @Schema(description = "绑定时间")
    private LocalDateTime createTime;
}
