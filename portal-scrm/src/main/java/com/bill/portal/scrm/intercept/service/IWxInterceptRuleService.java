package com.bill.portal.scrm.intercept.service;

import com.bill.portal.scrm.intercept.req.WxInterceptRuleSaveReq;
import com.bill.portal.scrm.intercept.vo.WxInterceptRuleVo;
import me.chanjar.weixin.common.error.WxErrorException;

import java.util.List;

/**
 * 企微聊天敏感词规则管理服务（代理企微 externalcontact/intercept_rule 系列接口）
 */
public interface IWxInterceptRuleService {

    /**
     * 规则列表（轻量摘要：ruleId/ruleName/createTime）
     *
     * @param corpId 企业ID
     */
    List<WxInterceptRuleVo> list(String corpId) throws WxErrorException;

    /**
     * 规则详情（含敏感词、语义规则、适用范围）
     *
     * @param corpId 企业ID
     * @param ruleId 规则ID
     */
    WxInterceptRuleVo detail(String corpId, String ruleId) throws WxErrorException;

    /**
     * 新建规则
     *
     * @return 企微侧规则ID
     */
    String create(WxInterceptRuleSaveReq req) throws WxErrorException;

    /**
     * 修改规则（适用范围按详情做差量，拆分为 add/remove 提交企微）
     */
    void update(WxInterceptRuleSaveReq req) throws WxErrorException;

    /**
     * 删除规则
     */
    void delete(String corpId, String ruleId) throws WxErrorException;
}
