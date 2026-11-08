package com.bill.portal.scrm.welcome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 企微欢迎语实体（容器：仅承载标题/企业）。
 *
 * <p>欢迎语即分时规则列表：内容与生效时间全部在
 * {@link WxWelcomeMsgRule}（wx_welcome_msg_rule）上。
 */
@Data
@TableName("wx_welcome_msg")
@Schema(description = "企微欢迎语实体")
public class WxWelcomeMsg extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "欢迎语标题")
    private String name;

    @Schema(description = "租户ID")
    private Long tenantId;

    @TableField(exist = false)
    @Schema(description = "绑定数量（分页列表回填）")
    private Long bindCount;

    @TableField(exist = false)
    @Schema(description = "规则数量（分页列表回填）")
    private Long ruleCount;

    @TableField(exist = false)
    @Schema(description = "绑定范围对象名称列表（分页列表回填）")
    private List<String> bindNames;
}
