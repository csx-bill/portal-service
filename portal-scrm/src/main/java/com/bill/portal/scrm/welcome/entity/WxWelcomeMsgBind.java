package com.bill.portal.scrm.welcome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 企微欢迎语绑定实体。
 *
 * <p>唯一键 (corp_id, target_type, target_id, welcome_msg_id)：
 * 同一成员/部门对同一条欢迎语仅一条绑定；时间不重叠的多条欢迎语
 * 可同时绑定同一目标。
 */
@Data
@TableName("wx_welcome_msg_bind")
@Schema(description = "企微欢迎语绑定实体")
public class WxWelcomeMsgBind extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "绑定类型：1=员工 2=部门")
    private Integer targetType;

    @Schema(description = "员工ID（wx_user.id）或部门ID（wx_department.id）")
    private Long targetId;

    @Schema(description = "主欢迎语ID")
    private Long welcomeMsgId;

    @Schema(description = "租户ID")
    private Long tenantId;
}
