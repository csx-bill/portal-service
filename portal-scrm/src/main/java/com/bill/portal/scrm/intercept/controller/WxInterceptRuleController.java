package com.bill.portal.scrm.intercept.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bill.portal.common.core.model.R;
import com.bill.portal.scrm.intercept.req.WxInterceptRuleSaveReq;
import com.bill.portal.scrm.intercept.service.IWxInterceptRuleService;
import com.bill.portal.scrm.intercept.vo.WxInterceptRuleVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 企微聊天敏感词规则管理（代理企微 externalcontact/*_intercept_rule 系列
 * 接口，规则数据以企微侧为唯一来源，本地不落库）
 */
@Tag(name = "企微聊天敏感词管理")
@Validated
@RestController
@RequestMapping("/scrm/intercept/rule")
@AllArgsConstructor
public class WxInterceptRuleController {

    private final IWxInterceptRuleService wxInterceptRuleService;

    @GetMapping("/list")
    @Operation(summary = "规则列表", description = "企微全量规则轻量摘要，完整配置见详情接口")
    public R<List<WxInterceptRuleVo>> list(@NotBlank @RequestParam("corpId") String corpId)
            throws WxErrorException {
        return R.success(wxInterceptRuleService.list(corpId));
    }

    @GetMapping("/{ruleId}")
    @Operation(summary = "规则详情", description = "含敏感词、语义规则、适用范围；适用范围仅返回应用可见范围内的成员和部门")
    public R<WxInterceptRuleVo> detail(@NotBlank @RequestParam("corpId") String corpId,
                                       @PathVariable("ruleId") String ruleId) throws WxErrorException {
        return R.success(wxInterceptRuleService.detail(corpId, ruleId));
    }

    @PostMapping
    @SaCheckPermission("intercept:add")
    @Operation(summary = "新建规则")
    public R<String> create(@Valid @RequestBody WxInterceptRuleSaveReq req) throws WxErrorException {
        return R.success(wxInterceptRuleService.create(req));
    }

    @PutMapping
    @SaCheckPermission("intercept:update")
    @Operation(summary = "修改规则", description = "敏感词整组覆盖（为空不调整）；语义规则传空数组清除；适用范围服务端按详情做差量增删")
    public R<Boolean> update(@Valid @RequestBody WxInterceptRuleSaveReq req) throws WxErrorException {
        wxInterceptRuleService.update(req);
        return R.success(true);
    }

    @DeleteMapping("/{ruleId}")
    @SaCheckPermission("intercept:delete")
    @Operation(summary = "删除规则", description = "仅可删除本应用创建的规则")
    public R<Boolean> delete(@NotBlank @RequestParam("corpId") String corpId,
                             @PathVariable("ruleId") String ruleId) throws WxErrorException {
        wxInterceptRuleService.delete(corpId, ruleId);
        return R.success(true);
    }
}
