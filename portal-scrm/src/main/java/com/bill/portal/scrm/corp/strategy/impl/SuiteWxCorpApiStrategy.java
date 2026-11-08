package com.bill.portal.scrm.corp.strategy.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bill.portal.scrm.core.annotation.ConditionalOnSuiteMode;
import com.bill.portal.scrm.corp.entity.WxCorpAuth;
import com.bill.portal.scrm.corp.service.IWxCorpAuthService;
import com.bill.portal.scrm.corp.strategy.WxCorpApiStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.config.impl.WxCpRedisTemplateConfigImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;


/**
 * 服务商应用策略实现
 * 仅在 xqt.wx.mode=SUITE 时生效
 */
@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnSuiteMode
public class SuiteWxCorpApiStrategy implements WxCorpApiStrategy {

    private final IWxCorpAuthService wxCorpAuthService;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public WxCpService getWxCpService(String corpId) {
        WxCorpAuth wxCorpAuth = wxCorpAuthService.getOne(Wrappers.<WxCorpAuth>lambdaQuery().eq(WxCorpAuth::getCorpId, corpId));
        WxCpRedisTemplateConfigImpl configStorage = new WxCpRedisTemplateConfigImpl(
                stringRedisTemplate
        );
        configStorage.setCorpId(corpId);
        configStorage.setAgentId(wxCorpAuth.getAgentId());
        configStorage.setCorpSecret(wxCorpAuth.getPermanentCode());
        WxCpServiceImpl service = new WxCpServiceImpl();
        service.setWxCpConfigStorage(configStorage);
        return service;
    }
}
