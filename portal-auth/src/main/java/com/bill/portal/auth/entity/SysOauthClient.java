package com.bill.portal.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 终端信息实体
 */
@Data
@TableName(value = "sys_oauth_client")
public class SysOauthClient implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
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

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建人")
    private String createBy;

    @TableField(fill = FieldFill.UPDATE)
    @Schema(description = "修改人")
    private String updateBy;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "最后更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @Schema(description = "删除标记,1:已删除,0:正常")
    private Integer delFlag;
}
