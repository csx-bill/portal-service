package com.bill.portal.scrm.inheritance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 单个客户的分配结果（errcode=0 仅表示已发起接替流程，
 * 24 小时后自动接替，不代表最终成功）
 */
@Data
@Schema(description = "单个客户的分配结果")
public class CustomerTransferResultDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "外部联系人 userid")
    private String externalUserid;

    @Schema(description = "该客户的分配结果码，0=已开始接替流程")
    private Integer errcode;
}
