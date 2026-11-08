package com.bill.portal.scrm.groupchat.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企微客户群成员实体
 */
@Data
@TableName(value = "wx_group_chat_member")
@Schema(description = "企微客户群成员实体")
public class WxGroupChatMember extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "客户群ID")
    private String chatId;

    @Schema(description = "群成员id")
    private String userId;

    @Schema(description = "成员类型。\n" +
            "1 - 企业成员\n" +
            "2 - 外部联系人")
    private Integer type;

    @Schema(description = "外部联系人在微信开放平台的唯一身份标识（微信unionid）")
    private String unionId;

    @Schema(description = "入群时间")
    private LocalDateTime joinTime;

    @Schema(description = "入群方式。\n" +
            "1 - 由群成员邀请入群（直接邀请入群）\n" +
            "2 - 由群成员邀请入群（通过邀请链接入群）\n" +
            "3 - 通过扫描群二维码入群")
    private Integer joinScene;

    @Schema(description = "邀请者的userid")
    private String invitorUserId;

    @Schema(description = "在群里的昵称")
    private String groupNickname;

    @Schema(description = "名字")
    private String name;

    @Schema(description = "同步版本号 (每次变更递增，用于乐观锁控制)")
    private Long syncVersion;

    @Schema(description = "租户ID")
    private Long tenantId;

}
