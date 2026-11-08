package com.bill.portal.scrm.welcome.req;

import com.bill.portal.scrm.welcome.dto.WelcomeBindItem;
import com.bill.portal.scrm.welcome.dto.WelcomeTimeItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 欢迎语保存参数（POST /scrm/welcome：id 为空新增，否则更新）。
 *
 * <p>欢迎语即分时规则列表：主行仅作容器（标题/企业/绑定），
 * 内容与生效时间全部在 timeItems 上；分时子项整组替换；
 * 绑定按唯一键 (corp_id, target_type, target_id) 改指向。
 */
@Data
@Schema(description = "欢迎语保存参数")
public class WxWelcomeMsgSaveReq {

    @Schema(description = "主键ID（空=新增）")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "欢迎语标题")
    private String name;

    @Schema(description = "分时规则（整组替换，至少一条）")
    private List<WelcomeTimeItem> timeItems;

    @Schema(description = "绑定列表（整组替换；跨欢迎语冲突时默认报错并提示归属）")
    private List<WelcomeBindItem> binds;

    @Schema(description = "绑定冲突时是否强制改指向：false-报错并提示归属（默认），true-直接改绑")
    private Boolean forceBind;
}
