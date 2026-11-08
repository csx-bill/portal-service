package com.bill.portal.scrm.callback.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.bill.portal.scrm.callback.strategy.WxCallbackStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 企微回调接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/scrm/cp/self")
public class CallbackController {

    @Autowired
    private WxCallbackStrategy wxCallbackStrategy;

    /**
     * 验证 URL 有效性 (GET 请求)
     * 当在企微后台配置或修改回调 URL 时，企微会发送验证请求，成功后才能保存。
     *
     * @param msgSignature 企微加密签名
     * @param timestamp    时间戳
     * @param nonce        随机数
     * @param echostr      加密的随机字符串
     * @return 解密后的 echostr 明文
     */
    @SaIgnore
    @GetMapping("/callback/{app}")
    public String verifyUrl(@PathVariable("app") String app,
                            @RequestParam("msg_signature") String msgSignature,
                            @RequestParam("timestamp") String timestamp,
                            @RequestParam("nonce") String nonce,
                            @RequestParam("echostr") String echostr) {

        log.info("app:{},msg_signature:{},timestamp:{},nonce:{},echostr:{}",app,msgSignature,timestamp,nonce,echostr);
        return wxCallbackStrategy.verifyUrl(app, msgSignature, timestamp, nonce, echostr);
    }

    /**
     * 接收事件推送 (POST 请求)
     * 企微会将事件数据以 XML 格式加密推送过来。
     *
     * @param requestBody  加密的 XML 请求体
     * @param msgSignature 签名
     * @param timestamp    时间戳
     * @param nonce        随机数
     * @return "success" (企微要求收到事件后必须返回 success 或空字符串，否则会重试)
     */
    @SaIgnore
    @PostMapping("/callback/{app}")
    public String receiveEvent(
            @PathVariable("app") String app,
            @RequestBody String requestBody,
            @RequestParam("msg_signature") String msgSignature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce) {

        log.info("app:{},msg_signature:{},timestamp:{},nonce:{}",app,msgSignature,timestamp,nonce);

        try {
            wxCallbackStrategy.decrypt(app,requestBody,msgSignature,timestamp,nonce);
        } catch (Exception e) {
            log.error("回调处理异常:{}", e);
        }
        return "success";
    }

}
