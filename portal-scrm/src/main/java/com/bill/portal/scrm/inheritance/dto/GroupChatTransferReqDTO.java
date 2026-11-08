package com.bill.portal.scrm.inheritance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 客户群群主转接请求（在职继承 onjob_transfer / 离职继承 groupchat/transfer 共用）
 */
@Data
@Schema(description = "客户群群主转接请求")
public class GroupChatTransferReqDTO {

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "待转群主的客户群 ID 列表，数量1~100（离职继承时群主须已离职）")
    private List<String> chatIdList;

    @Schema(description = "新群主的企微 userid")
    private String newOwner;
}
