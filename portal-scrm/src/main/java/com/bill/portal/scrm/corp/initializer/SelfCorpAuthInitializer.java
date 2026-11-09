package com.bill.portal.scrm.corp.initializer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bill.portal.common.core.tenant.TenantBroker;
import com.bill.portal.scrm.core.annotation.ConditionalOnSelfMode;
import com.bill.portal.scrm.core.constant.WxConstants;
import com.bill.portal.scrm.corp.entity.WxCorpAuth;
import com.bill.portal.scrm.corp.mapper.WxCorpAuthMapper;
import com.bill.portal.scrm.corp.strategy.WxCorpApiStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.WxCpAgentService;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.WxCpAgent;
import me.chanjar.weixin.cp.config.WxCpConfigStorage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 自建应用(内部应用)企业授权信息自动初始化器
 *
 * 核心职责：
 * 1. 在项目启动完成后，自动读取配置文件中的企业 corpId。
 * 2. 调用企微 API 获取企业真实名称。
 * 3. 将企业信息初始化到公共的 wx_corp_auth 授权表中，供后续通讯录同步等业务使用。
 *
 * 执行时机：
 * 监听 ApplicationReadyEvent，确保 Spring 容器完全就绪、所有 Bean 初始化完毕后执行。
 *
 * 生效条件：
 * 仅在 xqt.wx.mode=SELF (自建应用模式) 时才会被 Spring 容器加载和执行。
 */
@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnSelfMode
public class SelfCorpAuthInitializer {

    private final WxCorpAuthMapper wxCorpAuthMapper;

    private final WxCorpApiStrategy wxCorpApiStrategy;

    private final Environment environment;

    /**
     * 监听应用启动就绪事件，执行初始化逻辑
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional(rollbackFor = Exception.class)
    public void initSelfCorpAuth() throws WxErrorException {

        TenantBroker.runAs(1L, (id) -> {
            // 1. 读取配置文件中的企业 corpId
            WxCpService wxCpService = wxCorpApiStrategy.getWxCpService(null);
            WxCpConfigStorage wxCpConfigStorage = wxCpService.getWxCpConfigStorage();
            String corpId = wxCpConfigStorage.getCorpId();
            if (StringUtils.isBlank(corpId)) {
                log.warn("--- [SELF初始化] 配置文件中未发现 corpId，跳过初始化 ---");
                return;
            }

            // 2. 查询数据库，判断该企业是否已初始化过 (保证幂等性)
            WxCorpAuth existAuth = wxCorpAuthMapper.selectOne(
                    new LambdaQueryWrapper<WxCorpAuth>().eq(WxCorpAuth::getCorpId, corpId)
            );

            // 3. 根据查询结果执行插入或更新操作
            if (existAuth == null) {

                WxCpAgentService agentService = wxCpService.getAgentService();
                Long agentId = wxCpConfigStorage.getAgentId();
                WxCpAgent wxCpAgent = agentService.get(agentId);

                // 首次启动：插入新的授权记录
                WxCorpAuth newAuth = new WxCorpAuth();
                newAuth.setCorpId(corpId);
                // 企业名称
                newAuth.setCorpName(environment.getProperty("xqt.wx.self-corp-name"));
                newAuth.setCorpType("SELF"); // 标记为自建应用类型
                newAuth.setAuthStatus(1);    // 授权状态正常
                newAuth.setCreateTime(LocalDateTime.now());
                newAuth.setAgentId(agentId);
                newAuth.setAgentName(wxCpAgent.getName());
                newAuth.setSquareLogoUrl(wxCpAgent.getSquareLogoUrl());
                newAuth.setApp(WxConstants.XQT_SCRM);
                newAuth.setTenantId(1L);
                wxCorpAuthMapper.insert(newAuth);
                log.info("--- [SELF初始化] 成功写入企业[{}]授权信息 ---", corpId);
            }
        });

    }
}
