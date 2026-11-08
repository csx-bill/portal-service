package com.bill.portal.common.core.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.oauth2.exception.SaOAuth2AccessTokenException;
import com.bill.portal.common.core.model.R;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * 全局异常处理器
 */
@Hidden
@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;



    @ExceptionHandler(SaOAuth2AccessTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<?> handleCheckedException(SaOAuth2AccessTokenException e) {
        log.error("处理请求异常", e);
        return R.fail(401,e.getMessage());
    }

    /**
     * Sa-Token 未登录/Token 无效
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<?> handleNotLoginException(NotLoginException e) {
        log.warn("未登录或 token 无效: {}", e.getMessage());
        return R.fail(401, "未登录或 token 无效");
    }

    /**
     * Sa-Token 缺少权限
     */
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<?> handleNotPermissionException(NotPermissionException e) {
        log.warn("无访问权限: {}", e.getMessage());
        return R.fail(403, "无访问权限：" + e.getPermission());
    }

    /**
     * Sa-Token 缺少角色
     */
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<?> handleNotRoleException(NotRoleException e) {
        log.warn("缺少角色: {}", e.getMessage());
        return R.fail(403, "无访问权限");
    }

    /**
     * 参数校验异常（@Valid + DTO 校验注解不通过）。
     * 返回第一条字段错误的提示文案，前端 message 可直接展示
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null ? "参数校验失败" : fieldError.getDefaultMessage();
        log.warn("参数校验失败: {}", message);
        return R.fail(400, message);
    }

    /**
     * 参数校验异常（@RequestParam 等非 @RequestBody 参数上的约束注解，
     * 走框架内置方法校验，抛出的异常类型与上面不同）
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<?> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        String message = e.getAllErrors().isEmpty()
                ? "参数校验失败"
                : e.getAllErrors().get(0).getDefaultMessage();
        log.warn("参数校验失败: {}", message);
        return R.fail(400, message);
    }

    /**
     * 校验异常（业务主动抛出）。
     * message 约定为消息 key（如 "dict.system.cannotDelete"），
     * 经 MessageSource 按 Accept-Language 翻译；非 key 的原文原样返回
     */
    @ExceptionHandler(CheckedException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<?> handleCheckedException(CheckedException e) {
        String message = messageSource.getMessage(
                e.getMessage(), null, e.getMessage(), LocaleContextHolder.getLocale());
        log.warn("业务校验异常: {}", message);
        return R.fail(message);
    }

//    /**
//     * 企微异常
//     * @param e
//     * @return
//     */
//    @ExceptionHandler(WxErrorException.class)
//    @ResponseStatus(HttpStatus.OK)
//    public R<?> handleWxErrorException(WxErrorException e) {
//        log.error("处理请求异常", e);
//        return R.fail(e.getMessage());
//    }


    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<?> handleException(Exception e) {
        log.error("处理请求异常", e);
        return R.fail(e.getMessage());
    }
}
