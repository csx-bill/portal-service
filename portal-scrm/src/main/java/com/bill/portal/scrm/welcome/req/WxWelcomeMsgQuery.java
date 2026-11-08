package com.bill.portal.scrm.welcome.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 欢迎语分页查询参数（corpId 精确 / name 模糊，仅非空字段生效；
 * 仅返回主欢迎语，分时子项随详情返回）。
 */
@Data
@Schema(description = "欢迎语分页查询参数")
public class WxWelcomeMsgQuery {

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "欢迎语标题（模糊匹配）")
    private String name;

    @Schema(description = "绑定成员的企微userid（通讯录树过滤用，匹配绑定表 target_type=USER）")
    private String sender;

    @Schema(description = "绑定部门ID（通讯录树过滤用，匹配绑定表 target_type=DEPT）")
    private Long deptId;
}
