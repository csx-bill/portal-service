package com.bill.portal.scrm.contact.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.bill.portal.common.core.model.BaseEntity;
import com.bill.portal.common.core.datascope.DataScopeOwner;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@TableName(value = "wx_user", autoResultMap = true)
@DataScopeOwner(column = "user_id")
@Schema(description = "企微用户信息表")
public class WxUser extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "企微账号ID (企微唯一标识)")
    private String userId;

    @Schema(description = "用户姓名")
    private String name;

    @Schema(description = "手机号码")
    private String mobile;

    @TableField(typeHandler = Jackson3TypeHandler.class)
    @Schema(description = "所属部门ID列表(JSON数组)")
    private List<Long> departIds;

    @Schema(description = "主部门ID")
    private Long mainDepartment;

    @Schema(description = "职务信息")
    private String position;

    @Schema(description = "性别。0表示未定义，1表示男性，2表示女性。")
    private Integer gender;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "激活状态: 1=已激活，2=已禁用，4=未激活，5=退出企业")
    private Integer status;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "openUserId")
    private String openUserId;

    @Schema(description = "同步版本号 (每次变更递增，用于乐观锁控制)")
    private Long syncVersion;

    @TableField(typeHandler = Jackson3TypeHandler.class)
    @Schema(description = "企微用户原始完整数据快照")
    private Map<String, Object> extraData;

    @Schema(description = "租户ID")
    private Long tenantId;

}
