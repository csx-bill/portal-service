package com.bill.portal.scrm.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.bill.portal.common.core.model.BaseEntity;
import com.bill.portal.common.core.datascope.DataScopeOwner;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 企微客户实体
 */
@Data
@TableName(value = "wx_customer", autoResultMap = true)
@DataScopeOwner(column = "user_id")
@Schema(description = "企微客户实体")
public class WxCustomer extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "外部联系人的userid")
    private String externalUserId;

    @Schema(description = "外部联系人的名称")
    private String name;

    @Schema(description = "外部联系人头像")
    private String avatar;

    @Schema(description = "外部联系人的类型，1表示该外部联系人是微信用户，2表示该外部联系人是企业微信用户")
    private Integer type;

    @Schema(description = "性别 0-未知 1-男性 2-女性")
    private Integer gender;

    @Schema(description = "外部联系人在微信开放平台的唯一身份标识（微信unionid）")
    private String unionId;

    @Schema(description = "外部联系人的职位")
    private String position;

    @Schema(description = "外部联系人所在企业的简称")
    private String corpName;

    @Schema(description = "外部联系人所在企业的主体名称")
    private String corpFullName;

    @TableField(typeHandler = Jackson3TypeHandler.class)
    @Schema(description = "企微客户原始完整数据快照")
    private Map<String, Object> extraData;

    @Schema(description = "添加了此外部联系人的企业成员userid")
    private String userId;

    @Schema(description = "同步版本号 (每次变更递增，用于乐观锁控制)")
    private Long syncVersion;

    @Schema(description = "是否流失:0-正常,1-流失(好友关系解除)")
    private Integer lostFlag;

    @Schema(description = "流失发起方:1-客户主动删除,2-员工删除,3-接替失败,4-在职继承转接解除")
    private Integer lostBy;

    @Schema(description = "流失原因")
    private String lostReason;

    @Schema(description = "流失时间")
    private LocalDateTime lostTime;

    @Schema(description = "该成员添加此外部联系人的时间")
    private LocalDateTime followCreateTime;

    @Schema(description = "企业自定义的state参数（联系我/获客链接渠道标识）")
    private String followState;

    @Schema(description = "客户交接目标成员userid（交接/继承场景，流失行记录交接去向）")
    private String handoverToUserid;

    @Schema(description = "客户交接时间（发起/完成交接时写入）")
    private LocalDateTime handoverTime;

    @Schema(description = "租户ID")
    private Long tenantId;

}
