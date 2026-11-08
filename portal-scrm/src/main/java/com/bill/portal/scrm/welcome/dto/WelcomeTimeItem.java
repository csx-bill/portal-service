package com.bill.portal.scrm.welcome.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;

/**
 * 欢迎语分时规则（保存请求与详情响应共用；每条规则自带内容与生效时间，
 * 全天生效的兜底规则存为 effective_at=[1..7]、00:00:00~23:59:59）。
 */
@Data
@Schema(description = "欢迎语分时规则")
public class WelcomeTimeItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "子项ID（详情返回，保存时整组替换）")
    private Long id;

    @Schema(description = "规则名称")
    private String name;

    @Schema(description = "规则内容")
    private List<WelcomeContentItem> content;

    @Schema(description = "生效星期，如[1,2,3,4,5]，1=周一")
    private List<Integer> effectiveAt;

    @Schema(description = "生效开始时间（HH:mm:ss）")
    private LocalTime startTime;

    @Schema(description = "生效结束时间（HH:mm:ss）")
    private LocalTime endTime;
}
