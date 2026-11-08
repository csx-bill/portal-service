package com.bill.portal.scrm.welcome.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 欢迎语内容项（wx_welcome_msg.content JSON 数组元素，字段与群发内容同构）。
 */
@Data
@Schema(description = "欢迎语内容项")
public class WelcomeContentItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "消息类型：text-文本，image-图片，video-视频，link-链接")
    private String msgType;

    @Schema(description = "文本内容（msgType = text 时使用）")
    private String textContent;

    @Schema(description = "资源地址（image/video 使用，同时作为素材重传数据源）")
    private String url;

    @Schema(description = "文件名")
    private String filename;

    @Schema(description = "链接标题（link 使用）")
    private String linkTitle;

    @Schema(description = "链接描述（link 使用）")
    private String linkDesc;

    @Schema(description = "链接跳转URL（link 使用）")
    private String linkUrl;

    @Schema(description = "小程序标题（miniprogram 使用，最长 64 字节）")
    private String mpTitle;

    @Schema(description = "小程序appid（须为关联到企业的小程序应用）")
    private String mpAppid;

    @Schema(description = "小程序页面路径")
    private String mpPage;
}
