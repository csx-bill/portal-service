package com.bill.portal.scrm.inheritance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 客户群群主转接结果（failedChatList 为空表示全部成功）
 */
@Data
@Schema(description = "客户群群主转接结果")
public class GroupChatTransferRespDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "未成功继承的群列表，空 = 全部成功")
    private List<GroupChatTransferFailedDTO> failedChatList;
}
