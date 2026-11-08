package com.bill.portal.scrm.welcome.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 企微欢迎语分时规则实体（欢迎语即规则列表，每条规则自带内容与生效时间）。
 *
 * <p>全天生效的兜底规则存为 effective_at=[1..7]、00:00:00~23:59:59；
 * 发送侧按星期/时间匹配，命中即发，全天生效规则作为兜底。
 */
@Data
@TableName(value = "wx_welcome_msg_rule", autoResultMap = true)
@Schema(description = "企微欢迎语分时规则实体")
public class WxWelcomeMsgRule extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "所属欢迎语ID（wx_welcome_msg.id）")
    private Long welcomeMsgId;

    @Schema(description = "规则名称")
    private String name;

    @Schema(description = "规则内容JSON，含文本/图片/视频/链接")
    private String content;

    @Schema(description = "生效星期，如[1,2,3,4,5]，1=周一")
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<Integer> effectiveAt;

    @Schema(description = "生效开始时间")
    private LocalTime startTime;

    @Schema(description = "生效结束时间")
    private LocalTime endTime;

    @Schema(description = "企微临时素材media_id（发送时使用，过期需按 media_url 重传）")
    private String mediaId;

    @Schema(description = "media_id过期时间")
    private LocalDateTime mediaExpiresAt;

    @Schema(description = "媒体本地/云存储地址（重传素材的数据源）")
    private String mediaUrl;

    @Schema(description = "排序（匹配顺序）")
    private Integer sort;

    @Schema(description = "租户ID")
    private Long tenantId;
}
