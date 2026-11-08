package com.bill.portal.scrm.callback.strategy;

public interface WxCallbackStrategy {
    /**
     * 验证回调URL有效性 (GET请求)
     *
     * @param app 应用
     * @param msgSignature 签名
     * @param timestamp    时间戳
     * @param nonce        随机数
     * @param echostr      加密的echostr
     * @return 解密后的明文echostr，直接返回给企微
     */
    String verifyUrl(String app,String msgSignature, String timestamp, String nonce, String echostr);


    /**
     * 解密回调报文 (POST请求)
     *
     * @param app 应用
     * @param requestBody  POST请求体 (企微发送的加密XML)
     * @param msgSignature 企业微信加密签名
     * @param timestamp    时间戳
     * @param nonce        随机数
     */
    void decrypt(String app,String requestBody, String msgSignature, String timestamp, String nonce);
}
