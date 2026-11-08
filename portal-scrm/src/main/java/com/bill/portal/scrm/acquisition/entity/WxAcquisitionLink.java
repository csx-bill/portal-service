package com.bill.portal.scrm.acquisition.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 获客链接实体
 */
@Data
@TableName(value = "wx_acquisition_link", autoResultMap = true)
@Schema(description = "获客链接实体")
public class WxAcquisitionLink extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "获客链接ID（企微返回）")
    private String linkId;

    @Schema(description = "链接名称")
    private String linkName;

    @Schema(description = "链接URL")
    private String url;

    @Schema(description = "是否跳过验证，默认true")
    private Boolean skipVerify;

    @Schema(description = "是否标记来源为当前应用链接")
    private Boolean markSource;

    @Schema(description = "客户渠道（本地标记，取 sys_dict customer_channel 字典项值）")
    private String customerChannel;

    @Schema(description = "使用范围-成员ID列表(JSON数组)")
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> rangeUserList;

    @Schema(description = "使用范围-部门ID列表(JSON数组)")
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<Integer> rangeDepartmentList;

    @Schema(description = "优先级类型: 1-企业全员优先, 2-指定成员列表")
    private Integer priorityType;

    @Schema(description = "优先级成员ID列表(JSON数组)")
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> priorityUseridList;

    @Schema(description = "同步版本号")
    private Long syncVersion;

    @Schema(description = "租户ID")
    private Long tenantId;

}
