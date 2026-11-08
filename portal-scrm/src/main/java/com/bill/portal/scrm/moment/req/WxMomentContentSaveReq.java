package com.bill.portal.scrm.moment.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class WxMomentContentSaveReq {

    @Schema(description = "消息类型：text-文本，image-图片，video-视频，link-链接")
    private String msgType;

    @Schema(description = "文本内容（msg_type = text 时使用）")
    private String textContent;

    @Schema(description = "链接标题")
    private String linkTitle;

    @Schema(description = "链接描述")
    private String linkDesc;

    @Schema(description = "链接跳转URL")
    private String linkUrl;

    @Schema(description = "文件名")
    private String filename;

    @Schema(description = "资源地址")
    private String url;

    @Schema(description = "附件排序（从小到大，最多9个）")
    private String sort;


}
