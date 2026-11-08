package com.bill.portal.scrm.mass.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 企微群发内容记录实体
 */
@Data
@TableName(value = "wx_mass_content_log", autoResultMap = true)
@Schema(description = "企微群发内容记录实体")
public class WxMassContentLog extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;


    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "调用ID")
    private Long invokeId;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "消息类型：text-文本，image-图片，video-视频，file-文件，link-链接，miniprogram-小程序")
    private String msgType;

    @Schema(description = "文本内容（msg_type = text 时使用）")
    private String textContent;

    @Schema(description = "链接标题")
    private String linkTitle;

    @Schema(description = "链接描述")
    private String linkDesc;

    @Schema(description = "链接跳转URL")
    private String linkUrl;

    @Schema(description = "小程序卡片标题")
    private String miniTitle;

    @Schema(description = "小程序页面路径")
    private String miniPage;

    @Schema(description = "小程序appid")
    private String miniAppid;

    @Schema(description = "文件名")
    private String filename;

    @Schema(description = "资源地址")
    private String url;

    @Schema(description = "附件排序（从小到大，最多9个）")
    private String sort;

    @Schema(description = "租户ID")
    private Long tenantId;
}
