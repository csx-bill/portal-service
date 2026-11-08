package com.bill.portal.auth.handler;

import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.oauth2.SaOAuth2Manager;
import cn.dev33.satoken.oauth2.consts.GrantType;
import cn.dev33.satoken.oauth2.consts.SaOAuth2Consts;
import cn.dev33.satoken.oauth2.data.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.data.model.request.RequestAuthModel;
import cn.dev33.satoken.oauth2.exception.SaOAuth2Exception;
import cn.dev33.satoken.oauth2.granttype.handler.PasswordGrantTypeHandler;
import cn.dev33.satoken.secure.BCrypt;
import com.bill.portal.common.core.constant.OAuth2Constants;
import com.bill.portal.common.core.security.UserInfo;
import com.bill.portal.system.api.RemoteUserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 自定义 Password Grant_Type 授权模式处理器认证过程
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomPasswordGrantTypeHandler extends PasswordGrantTypeHandler {

    private final RemoteUserClient remoteUserClient;

    @Override
    public AccessTokenModel getAccessToken(SaRequest req, String clientId, List<String> scopes) {
        String username = req.getParamNotNull(SaOAuth2Consts.Param.username);
        String password = req.getParamNotNull(SaOAuth2Consts.Param.password);
        UserInfo userInfo = remoteUserClient.info(username).getData();
        // 校验密码
        if (Objects.isNull(userInfo) || !BCrypt.checkpw(password, userInfo.getPassword())){
            throw (new SaOAuth2Exception("登录失败")).setCode(30161);
        } else {
            RequestAuthModel ra = new RequestAuthModel();
            ra.clientId = clientId;
            ra.loginId = userInfo.getId();
            ra.scopes = scopes;
            // 扩展参数：登录用户名
            AccessTokenModel at = SaOAuth2Manager.getDataGenerate().generateAccessToken(ra, true, (atm) -> {
                atm.grantType = GrantType.password;
                // 扩展参数：登录用户名，随 access_token 响应返回给客户端
                Map<String, Object> extraData = new LinkedHashMap<>();
                extraData.put(OAuth2Constants.EXTRA_DATA_USERNAME, userInfo.getUsername());
                atm.extraData = extraData;
            });
            return at;
        }
    }
}
