package com.bill.portal.scrm.corp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 企微企业授权管理表
 * 无论是自建应用还是服务商应用，所有业务模块均通过此表获取企业基础信息
 */
@Data
@TableName("wx_corp_auth")
@Schema(description = "企微企业授权管理表")
public class WxCorpAuth extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID (企微唯一标识)")
    private String corpId;

    @Schema(description = "企业名称")
    private String corpName;

    @Schema(description = "企业应用id")
    private Long agentId;

    @Schema(description = "企业应用名称")
    private String agentName;

    @Schema(description = "企业应用方形头像")
    private String squareLogoUrl;

    @Schema(description = "应用")
    private String app;

    @Schema(description = "服务商应用的secret")
    private String permanentCode;

    @Schema(description = "应用类型: SELF-自建应用, SUITE-服务商应用")
    private String corpType;

    @Schema(description = "授权状态: 0-已取消/过期, 1-正常授权")
    private Integer authStatus;

    @Schema(description = "租户ID")
    private Long tenantId;
}

