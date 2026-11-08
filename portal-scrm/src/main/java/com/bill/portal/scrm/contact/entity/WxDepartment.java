package com.bill.portal.scrm.contact.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@TableName(value = "wx_department", autoResultMap = true)
@Schema(description = "企微部门信息表")
public class WxDepartment extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "企微部门ID (企微唯一标识)")
    private Long deptId;

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "英文名称")
    private String nameEn;

    @Schema(description = "企微父部门ID (根部门为1)")
    private Long parentId;

    @Schema(description = "部门排序值")
    @TableField(value = "`order`")
    private Long order;

    @TableField(typeHandler = Jackson3TypeHandler.class)
    @Schema(description = "部门负责人userid列表(JSON数组)")
    private List<String> departmentLeader;

    @Schema(description = "同步版本号 (每次变更递增，用于乐观锁控制)")
    private Long syncVersion;

    @Schema(description = "租户ID")
    private Long tenantId;
}
