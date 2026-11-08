package com.bill.portal.common.core.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一 API 响应结果封装
 */
@Data
public class R<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;

    /**
     * 成功（无数据返回）
     */
    public static <T> R<T> success() {
        R<T> result = new R<>();
        result.setCode(0);
        result.setMessage("SUCCESS");
        return result;
    }

    /**
     * 成功（有数据返回）
     */
    public static <T> R<T> success(T data) {
        R<T> result = new R<>();
        result.setCode(0);
        result.setMessage("SUCCESS");
        result.setData(data);
        return result;
    }

    /**
     * 成功（自定义返回信息）
     */
    public static <T> R<T> success(String message, T data) {
        R<T> result = new R<>();
        result.setCode(0);
        result.setMessage(message);
        result.setData(data);
        return result;
    }


    /**
     * 失败（使用默认失败状态码）
     */
    public static <T> R<T> fail(String message) {
        R<T> result = new R<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败（使用自定义枚举状态码）
     */
//    public static <T> R<T> fail(ResultCode resultCode) {
//        R<T> result = new R<>();
//        result.setCode(resultCode.getCode());
//        result.setMessage(resultCode.getMessage());
//        return result;
//    }

    /**
     * 失败（使用自定义状态码 + 自定义信息）
     */
    public static <T> R<T> fail(Integer code, String message) {
        R<T> result = new R<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

}
