package com.bill.portal.auth.config;

import cn.dev33.satoken.oauth2.config.SaOAuth2ServerConfig;
import cn.dev33.satoken.oauth2.strategy.SaOAuth2Strategy;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * OAuth2-Server 端数据互通（官方方式，见 sa-token 文档）：
 * 重写 Access-Token 创建策略，签发的 access_token 直接使用账号的会话令牌
 * （StpUtil.getOrCreateLoginSession：已有会话复用其令牌值，否则创建登录会话）。
 *
 * 业务接口的 @SaCheckPermission 等 StpUtil 注解因此天然识别 Bearer token，
 * 授权处理器中无需再做任何手动桥接。
 *
 * 语义说明（官方文档提示）：
 * - 同一账号在所有 Client 拿到同一个令牌值，登录态全局共享（踢人下线全局生效）；
 * - refresh_token 刷新不再轮换令牌值，仅续期 oauth2 侧过期时间；
 * - 令牌生命周期随会话（sa-token.timeout）管理。
 */
@Configuration
public class SaTokenConfigure {

    @Autowired
    public void configOAuth2Server(SaOAuth2ServerConfig oauth2Server) {
        SaOAuth2Strategy.instance.createAccessToken = (clientId, loginId, scopes) ->
                StpUtil.getOrCreateLoginSession(loginId);
    }
}
