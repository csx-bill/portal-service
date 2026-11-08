package com.bill.portal.scrm.corp.strategy.impl;

import com.binarywang.spring.starter.wxjava.cp.service.WxCpMultiServices;
import com.bill.portal.scrm.core.annotation.ConditionalOnSelfMode;
import com.bill.portal.scrm.core.constant.WxConstants;
import com.bill.portal.scrm.corp.strategy.WxCorpApiStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.api.WxCpService;
import org.springframework.stereotype.Component;


/**
 * 自建应用策略实现
 * 仅在 xqt.wx.mode=SELF 时生效
 */
@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnSelfMode
public class SelfWxCorpApiStrategy implements WxCorpApiStrategy {

    private final WxCpMultiServices wxCpMultiServices;

    @Override
    public WxCpService getWxCpService(String corpId) {
        return wxCpMultiServices.getWxCpService(WxConstants.XQT_SCRM);
    }
}
