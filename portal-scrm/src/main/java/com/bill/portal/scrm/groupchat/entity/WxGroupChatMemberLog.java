package com.bill.portal.scrm.groupchat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 群成员变动记录实体（入群/退群流水，用于群统计）
 */
@Data
@TableName("wx_group_chat_member_log")
@Schema(description = "群成员变动记录实体")
public class WxGroupChatMemberLog {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "客户群ID")
    private String chatId;

    @Schema(description = "成员userid")
    private String userid;

    @Schema(description = "成员名称")
    private String name;

    @Schema(description = "变动类型:1-入群,2-退群")
    private Integer changeType;

    @Schema(description = "变动时间")
    private LocalDateTime changeTime;

    @Schema(description = "同步版本号")
    private Long syncVersion;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
