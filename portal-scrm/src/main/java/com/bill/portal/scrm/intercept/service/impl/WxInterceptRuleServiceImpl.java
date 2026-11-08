package com.bill.portal.scrm.intercept.service.impl;

import com.bill.portal.scrm.corp.strategy.WxCorpApiStrategy;
import com.bill.portal.scrm.intercept.req.WxInterceptRuleSaveReq;
import com.bill.portal.scrm.intercept.service.IWxInterceptRuleService;
import com.bill.portal.scrm.intercept.vo.WxInterceptRuleVo;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.WxCpExternalContactService;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.external.interceptrule.ApplicableRange;
import me.chanjar.weixin.cp.bean.external.interceptrule.WxCpInterceptRule;
import me.chanjar.weixin.cp.bean.external.interceptrule.WxCpInterceptRuleAddRequest;
import me.chanjar.weixin.cp.bean.external.interceptrule.WxCpInterceptRuleInfo;
import me.chanjar.weixin.cp.bean.external.interceptrule.WxCpInterceptRuleList;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 企微聊天敏感词规则管理实现。
 * 规则数据以企微侧为唯一来源，本地不落库；
 * 企微的修改接口仅支持增量增删适用范围（add/remove_applicable_range），
 * 因此修改时先查详情、按整组范围做差量再提交。
 */
@Service
@RequiredArgsConstructor
public class WxInterceptRuleServiceImpl implements IWxInterceptRuleService {

    private final WxCorpApiStrategy wxCorpApiStrategy;

    private WxCpExternalContactService externalContactService(String corpId) {
        WxCpService wxCpService = wxCorpApiStrategy.getWxCpService(corpId);
        return wxCpService.getExternalContactService();
    }

    @Override
    public List<WxInterceptRuleVo> list(String corpId) throws WxErrorException {
        WxCpInterceptRuleList ruleList = externalContactService(corpId).getInterceptRuleList();
        List<WxInterceptRuleVo> result = new ArrayList<>();
        for (WxCpInterceptRuleList.Rule rule : Optional.ofNullable(ruleList.getRuleList()).orElse(List.of())) {
            WxInterceptRuleVo vo = new WxInterceptRuleVo();
            vo.setRuleId(rule.getRuleId());
            vo.setRuleName(rule.getRuleName());
            vo.setCreateTime(toLocalDateTime(rule.getCreateTime()));
            result.add(vo);
        }
        return result;
    }

    @Override
    public WxInterceptRuleVo detail(String corpId, String ruleId) throws WxErrorException {
        WxCpInterceptRuleInfo info = externalContactService(corpId).getInterceptRuleDetail(ruleId);
        WxCpInterceptRuleInfo.Rule rule = info.getRule();
        WxInterceptRuleVo vo = new WxInterceptRuleVo();
        if (rule != null) {
            vo.setRuleId(rule.getRuleId());
            vo.setRuleName(rule.getRuleName());
            vo.setWordList(rule.getWordList());
            vo.setSemanticsList(rule.getSemanticsList());
            vo.setInterceptType(rule.getInterceptType());
            ApplicableRange range = rule.getApplicableRange();
            if (range != null) {
                vo.setUserList(range.getUserList());
                vo.setDepartmentList(range.getDepartmentList());
            }
            vo.setCreateTime(toLocalDateTime(rule.getCreateTime()));
        }
        return vo;
    }

    @Override
    public String create(WxInterceptRuleSaveReq req) throws WxErrorException {
        WxCpInterceptRuleAddRequest addRequest = WxCpInterceptRuleAddRequest.builder()
                .ruleName(req.getRuleName())
                .wordList(req.getWordList())
                // 新建语义规则仅在非空时提交
                .semanticsList(isEmpty(req.getSemanticsList()) ? null : req.getSemanticsList())
                .interceptType(req.getInterceptType())
                .applicableRange(toApplicableRange(req.getUserList(), req.getDepartmentList()))
                .build();
        return externalContactService(req.getCorpId()).addInterceptRule(addRequest);
    }

    @Override
    public void update(WxInterceptRuleSaveReq req) throws WxErrorException {
        WxCpExternalContactService service = externalContactService(req.getCorpId());

        WxCpInterceptRule rule = WxCpInterceptRule.builder()
                .ruleId(req.getRuleId())
                .ruleName(req.getRuleName())
                // word_list 为空企微侧忽略（敏感词无法通过修改清空）
                .wordList(isEmpty(req.getWordList()) ? null : req.getWordList())
                .interceptType(req.getInterceptType())
                .build();
        // semanticsList 传了（含空数组）即整组覆盖，空数组表示清除全部语义规则
        if (req.getSemanticsList() != null) {
            WxCpInterceptRule.ExtraRule extraRule = new WxCpInterceptRule.ExtraRule();
            extraRule.setSemanticsList(req.getSemanticsList());
            rule.setExtraRule(extraRule);
        }

        // 适用范围差量：详情只返回应用可见范围内的节点，差量基于可见部分计算
        WxInterceptRuleVo current = detail(req.getCorpId(), req.getRuleId());
        Set<String> currentUsers = new HashSet<>(Optional.ofNullable(current.getUserList()).orElse(List.of()));
        Set<Integer> currentDepts = new HashSet<>(Optional.ofNullable(current.getDepartmentList()).orElse(List.of()));
        List<String> usersToAdd = diff(req.getUserList(), currentUsers);
        List<Integer> deptsToAdd = diff(req.getDepartmentList(), currentDepts);
        List<String> usersToRemove = diff(current.getUserList(), toSet(req.getUserList()));
        List<Integer> deptsToRemove = diff(current.getDepartmentList(), toSet(req.getDepartmentList()));
        if (!isEmpty(usersToAdd) || !isEmpty(deptsToAdd)) {
            rule.setAddApplicableRange(toApplicableRange(usersToAdd, deptsToAdd));
        }
        if (!isEmpty(usersToRemove) || !isEmpty(deptsToRemove)) {
            rule.setRemoveApplicableRange(toApplicableRange(usersToRemove, deptsToRemove));
        }

        service.updateInterceptRule(rule);
    }

    @Override
    public void delete(String corpId, String ruleId) throws WxErrorException {
        externalContactService(corpId).delInterceptRule(ruleId);
    }

    private ApplicableRange toApplicableRange(List<String> userList, List<Integer> departmentList) {
        ApplicableRange range = new ApplicableRange();
        range.setUserList(userList);
        range.setDepartmentList(departmentList);
        return range;
    }

    /** 提交值中不在 current 集合里的部分（保序） */
    private <T> List<T> diff(List<T> submitted, Set<T> current) {
        List<T> result = new ArrayList<>();
        for (T item : Optional.ofNullable(submitted).orElse(List.of())) {
            if (!current.contains(item)) {
                result.add(item);
            }
        }
        return result;
    }

    private <T> Set<T> toSet(List<T> list) {
        return new HashSet<>(Optional.ofNullable(list).orElse(List.of()));
    }

    private boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    private LocalDateTime toLocalDateTime(Long epochSeconds) {
        if (epochSeconds == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
    }
}
