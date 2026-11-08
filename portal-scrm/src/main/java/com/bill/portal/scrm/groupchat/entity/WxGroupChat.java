package com.bill.portal.scrm.groupchat.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.bill.portal.common.core.datascope.DataScopeOwner;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 企微客户群主实体
 */
@Data
@TableName(value = "wx_group_chat", autoResultMap = true)
@DataScopeOwner(column = "owner")
@Schema(description = "企微客户群主实体")
public class WxGroupChat extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "客户群ID")
    private String chatId;

    @Schema(description = "群名称")
    private String chatName;

    @Schema(description = "群创建时间（企微群详情返回）")
    private LocalDateTime groupCreateTime;

    @Schema(description = "群主ID")
    private String owner;

    @Schema(description = "群公告")
    private String notice;

    @Schema(description = "客户群跟进状态。\n" +
            "0 - 跟进人正常\n" +
            "1 - 跟进人离职\n" +
            "2 - 离职继承中\n" +
            "3 - 离职继承完成")
    private Integer status;

    @Schema(description = "群成员总数")
    private Integer memberNum;

    @Schema(description = "群成员版本号（update 回调 CurMemVer，事件一致性校验）")
    private Long memberVersion;

    @Schema(description = "是否已解散:0-否,1-已解散（dismiss 事件标记）")
    private Integer dismissFlag;

    @Schema(description = "解散时间")
    private LocalDateTime dismissTime;

    @Schema(description = "群管理员userid")
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> adminList;

    @Schema(description = "同步版本号 (每次变更递增，用于乐观锁控制)")
    private Long syncVersion;

    @Schema(description = "租户ID")
    private Long tenantId;

}
