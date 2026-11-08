package com.bill.portal.scrm.core.annotation;

import com.bill.portal.scrm.core.condition.WxModeCondition;
import org.springframework.context.annotation.Conditional;
import java.lang.annotation.*;

/**
 * 自建应用模式条件装配注解
 * 只有当 xqt.wx.mode=SELF 时，被此注解标记的类/方法才会被 Spring 容器管理
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(WxModeCondition.class)
public @interface ConditionalOnSelfMode {
}
