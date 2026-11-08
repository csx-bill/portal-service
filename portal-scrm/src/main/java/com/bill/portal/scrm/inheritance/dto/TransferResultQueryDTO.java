package com.bill.portal.scrm.inheritance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客户接替状态查询参数（在职/离职共用，游标分页）
 */
@Data
@Schema(description = "客户接替状态查询参数")
public class TransferResultQueryDTO {

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "原跟进成员的企微 userid")
    private String handoverUserid;

    @Schema(description = "接替成员的企微 userid")
    private String takeoverUserid;

    @Schema(description = "分页游标（不填查第一页）")
    private String cursor;
}
