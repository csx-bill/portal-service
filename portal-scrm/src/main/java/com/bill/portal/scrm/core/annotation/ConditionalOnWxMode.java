package com.bill.portal.scrm.core.annotation;

import com.bill.portal.scrm.core.condition.WxModeCondition;
import org.springframework.context.annotation.Conditional;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(WxModeCondition.class)
public @interface ConditionalOnWxMode {
    /**
     * 企微模式: SELF 或 SUITE
     */
    String value();
}
