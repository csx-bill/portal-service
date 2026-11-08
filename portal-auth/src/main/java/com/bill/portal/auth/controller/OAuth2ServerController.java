package com.bill.portal.auth.controller;

import cn.dev33.satoken.oauth2.processor.SaOAuth2ServerProcessor;
import com.bill.portal.common.core.model.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * OAuth2 Server端 控制器
 */
@RestController
@RequestMapping("/oauth2")
@Tag(name = "OAuth2授权")
@RequiredArgsConstructor
public class OAuth2ServerController {

    @RequestMapping("/authorize")
    @Operation(summary = "模式一：Code授权码 || 模式二：隐藏式")
    public R<?> authorize() {
        return R.success(SaOAuth2ServerProcessor.instance.authorize());
    }

    @RequestMapping("/doConfirm")
    @Operation(summary = "用户确认授权")
    public R<?> doConfirm() {
        return R.success(SaOAuth2ServerProcessor.instance.doConfirm());
    }

    @RequestMapping("/token")
    @Operation(summary = "Code 换 Access-Token || 模式三：密码式")
    public R<?> token() {
        return R.success(SaOAuth2ServerProcessor.instance.token());
    }

    @RequestMapping("/refresh")
    @Operation(summary = "Refresh-Token 刷新 Access-Token")
    public R<?> refresh() {
        return R.success(SaOAuth2ServerProcessor.instance.refresh());
    }

    @RequestMapping("/logout")
    @Operation(summary = "退出登录")
    public R<?> logout() {
        return R.success(SaOAuth2ServerProcessor.instance.revoke());
    }

    // 模式四：凭证式
    @RequestMapping("/client_token")
    @Operation(summary = "模式四：凭证式")
    public R<?> clientToken() {
        return R.success(SaOAuth2ServerProcessor.instance.clientToken());
    }

}

