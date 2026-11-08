package com.bill.portal.scrm.groupchat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "企微客户群分页查询参数")
public class WxGroupChatQuery {

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "群名称")
    private String chatName;

    @Schema(description = "群主ID")
    private String owner;

    @Schema(description = "部门ID（通讯录树过滤用，按群主命中部门及子部门下的成员）")
    private Long deptId;

}
