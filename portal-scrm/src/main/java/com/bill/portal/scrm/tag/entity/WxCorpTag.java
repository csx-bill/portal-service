package com.bill.portal.scrm.tag.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 企微客户标签实体
 */
@Data
@TableName(value = "wx_corp_tag", autoResultMap = true)
@Schema(description = "企微客户标签实体")
public class WxCorpTag extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;


    @Schema(description = "标签id")
    private String tagId;

    @Schema(description = "标签名称")
    private String tagName;

    @Schema(description = "标签组id")
    private String groupId;

    @Schema(description = "标签次序值。order值大的排序靠前")
    @TableField(value = "`order`")
    private Long order;

    @Schema(description = "同步版本号 (每次变更递增，用于乐观锁控制)")
    private Long syncVersion;

    @Schema(description = "租户ID")
    private Long tenantId;

}
