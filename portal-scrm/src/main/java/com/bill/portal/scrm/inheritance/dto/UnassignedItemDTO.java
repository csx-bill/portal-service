package com.bill.portal.scrm.inheritance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 待分配的离职成员客户项
 */
@Data
@Schema(description = "待分配的离职成员客户项")
public class UnassignedItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "离职成员的企微 userid")
    private String handoverUserid;

    @Schema(description = "外部联系人 userid")
    private String externalUserid;

    @Schema(description = "成员离职时间（时间戳，秒）")
    private Long dimissionTime;
}
