package com.bill.portal.scrm.welcome.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 欢迎语绑定项（保存请求与详情响应共用；唯一键 corp+target，重复绑定改指向）。
 */
@Data
@Schema(description = "欢迎语绑定项")
public class WelcomeBindItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "绑定类型：1=员工 2=部门")
    private Integer targetType;

    @Schema(description = "员工ID（wx_user.id）或部门ID（wx_department.id）")
    private Long targetId;

    @Schema(description = "绑定对象名称（详情返回时回填，保存时无需传）")
    private String targetName;
}
