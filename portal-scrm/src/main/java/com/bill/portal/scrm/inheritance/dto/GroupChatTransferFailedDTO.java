package com.bill.portal.scrm.inheritance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 未成功继承的客户群
 */
@Data
@Schema(description = "未成功继承的客户群")
public class GroupChatTransferFailedDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "客户群 ID")
    private String chatId;

    @Schema(description = "错误码")
    private Long errcode;

    @Schema(description = "错误描述")
    private String errmsg;
}
