package com.bill.portal.scrm.intercept.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 敏感词规则视图对象（列表项仅填充 ruleId/ruleName/createTime，
 * 完整字段在详情接口返回；适用范围仅返回应用可见范围内的成员和部门）
 */
@Data
@Schema(description = "敏感词规则")
public class WxInterceptRuleVo {

    @Schema(description = "规则ID（企微侧标识）")
    private String ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "敏感词列表")
    private List<String> wordList;

    @Schema(description = "语义规则列表：1-手机号 2-邮箱地址 3-红包")
    private List<Integer> semanticsList;

    @Schema(description = "拦截动作：1-警告并拦截发送 2-仅发警告")
    private Integer interceptType;

    @Schema(description = "适用范围-成员ID列表")
    private List<String> userList;

    @Schema(description = "适用范围-部门ID列表")
    private List<Integer> departmentList;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
