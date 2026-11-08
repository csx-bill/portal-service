package com.bill.portal.scrm.inheritance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 客户接替状态查询结果（游标分页）
 */
@Data
@Schema(description = "客户接替状态查询结果")
public class TransferResultRespDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "接替状态列表")
    private List<TransferResultItemDTO> customer;

    @Schema(description = "下一页游标")
    private String nextCursor;
}
