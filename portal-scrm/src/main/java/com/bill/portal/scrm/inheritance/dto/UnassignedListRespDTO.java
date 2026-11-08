package com.bill.portal.scrm.inheritance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 待分配的离职成员客户列表（游标分页）
 */
@Data
@Schema(description = "待分配的离职成员客户列表")
public class UnassignedListRespDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "记录列表")
    private List<UnassignedItemDTO> list;

    @Schema(description = "是否为最后一条记录")
    private Boolean isLast;

    @Schema(description = "下一页游标（查完为空字符串）")
    private String nextCursor;
}
