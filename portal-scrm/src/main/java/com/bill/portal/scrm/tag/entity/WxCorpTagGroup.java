package com.bill.portal.scrm.tag.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 企微客户标签组
 */
@Data
@TableName(value = "wx_corp_tag_group", autoResultMap = true)
@Schema(description = "企微客户标签组")
public class WxCorpTagGroup extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "标签组id")
    private String groupId;

    @Schema(description = "标签组名称")
    private String groupName;

    @Schema(description = "标签组次序值。order值大的排序靠前")
    @TableField(value = "`order`")
    private Long order;

    @TableField(typeHandler = Jackson3TypeHandler.class)
    @Schema(description = "企微标签组原始完整数据快照")
    private Map<String, Object> extraData;

    @Schema(description = "同步版本号 (每次变更递增，用于乐观锁控制)")
    private Long syncVersion;

    @Schema(description = "租户ID")
    private Long tenantId;

    @TableField(exist = false)
    @Schema(description = "标签列表")
    private List<WxCorpTag> tagList;

}
