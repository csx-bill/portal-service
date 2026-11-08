package com.bill.portal.scrm.intercept.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 敏感词规则新建/修改参数（ruleId 为空新建、非空修改）
 */
@Data
@Schema(description = "敏感词规则保存参数")
public class WxInterceptRuleSaveReq {

    @NotBlank
    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "规则ID（企微侧标识，修改时必填）")
    private String ruleId;

    @NotBlank
    @Size(max = 20)
    @Schema(description = "规则名称，1~20个utf8字符")
    private String ruleName;

    @Schema(description = "敏感词列表，每个1~32个utf8字符，最多300个；修改时为空表示不调整")
    private List<@NotBlank @Size(max = 32) String> wordList;

    @Schema(description = "语义规则列表：1-手机号 2-邮箱地址 3-红包；修改时传空数组清除全部语义规则，不传表示不调整")
    private List<@NotNull @Min(1) @Max(3) Integer> semanticsList;

    @NotNull
    @Min(1)
    @Max(2)
    @Schema(description = "拦截动作：1-警告并拦截发送 2-仅发警告")
    private Integer interceptType;

    @Schema(description = "适用范围-成员ID列表")
    private List<@NotBlank String> userList;

    @Schema(description = "适用范围-部门ID列表")
    private List<@NotNull Integer> departmentList;
}
