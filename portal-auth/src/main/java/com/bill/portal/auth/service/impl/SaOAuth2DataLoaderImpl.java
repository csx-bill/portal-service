package com.bill.portal.auth.service.impl;

import cn.dev33.satoken.oauth2.data.loader.SaOAuth2DataLoader;
import cn.dev33.satoken.oauth2.data.model.loader.SaClientModel;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bill.portal.auth.entity.SysOauthClient;
import com.bill.portal.auth.mapper.SysOauthClientMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Sa-Token OAuth2：自定义数据加载器
 */
@Slf4j
@Service
@AllArgsConstructor
public class SaOAuth2DataLoaderImpl implements SaOAuth2DataLoader {

    private final SysOauthClientMapper sysOauthClientMapper;
    @Override
    public SaClientModel getClientModel(String clientId) {
        SysOauthClient sysOauthClient = sysOauthClientMapper.selectOne(Wrappers.<SysOauthClient>lambdaQuery().eq(SysOauthClient::getClientId, clientId));
        SaClientModel saClientModel = new SaClientModel();
        saClientModel.setClientId(sysOauthClient.getClientId());
        saClientModel.setClientSecret(sysOauthClient.getClientSecret());
        saClientModel.setContractScopes(splitToList(sysOauthClient.getContractScopes()));
        saClientModel.setAllowGrantTypes(splitToList(sysOauthClient.getAllowGrantTypes()));
        saClientModel.setAllowRedirectUris(splitToList(sysOauthClient.getAllowRedirectUris()));
        return saClientModel;
    }

    private List<String> splitToList(String str) {
        if (StringUtils.isBlank(str)) {
            return Collections.emptyList();
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
