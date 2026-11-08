package com.bill.portal.scrm.groupchat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 群成员变动统计（指定日期的入群/退群数量）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "群成员变动统计")
public class GroupMemberStatisticDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "客户群ID（按企业聚合查询时使用）")
    private String chatId;

    @Schema(description = "入群数量")
    private Long addCount;

    @Schema(description = "退群数量")
    private Long delCount;
}
