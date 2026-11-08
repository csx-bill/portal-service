package com.bill.portal.scrm.inheritance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 客户接替状态项
 */
@Data
@Schema(description = "客户接替状态项")
public class TransferResultItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "外部联系人 userid")
    private String externalUserid;

    @Schema(description = "接替状态：1-接替完毕 2-等待接替 3-客户拒绝 4-接替成员客户达到上限 5-无记录")
    private Integer status;

    @Schema(description = "接替时间；等待接替时为未来的自动接替时间（时间戳，秒）")
    private Long takeoverTime;
}
