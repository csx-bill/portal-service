package com.bill.portal.scrm.inheritance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 客户分配请求（在职继承 transfer_customer / 离职继承 resigned/transfer_customer 共用）
 */
@Data
@Schema(description = "客户分配请求")
public class CustomerTransferReqDTO {

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "原跟进成员的企微 userid（离职继承时须为已离职用户）")
    private String handoverUserid;

    @Schema(description = "接替成员的企微 userid")
    private String takeoverUserid;

    @Schema(description = "客户 external_userid 列表，每次最多100个")
    private List<String> externalUseridList;

    @Schema(description = "转接成功后发给客户的消息（最多200字符，在职继承支持，不填用默认文案）")
    private String transferMsg;
}
