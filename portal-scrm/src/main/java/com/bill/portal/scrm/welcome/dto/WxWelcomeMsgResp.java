package com.bill.portal.scrm.welcome.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 企微欢迎语详情（GET /scrm/welcome/{id} 返回体）。
 *
 * <p>欢迎语即分时规则列表：主行仅承载标题/企业，内容与生效时间
 * 全部在 timeItems 上；binds 为绑定列表（含绑定对象名称）。
 */
@Data
@Schema(description = "企微欢迎语详情")
public class WxWelcomeMsgResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "欢迎语标题")
    private String name;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "分时规则列表（每条含内容与生效时间）")
    private List<WelcomeTimeItem> timeItems;

    @Schema(description = "绑定列表（含绑定对象名称）")
    private List<WelcomeBindItem> binds;
}
