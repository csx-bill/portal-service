package com.bill.portal.scrm.core.condition;

import com.bill.portal.scrm.core.annotation.ConditionalOnSelfMode;
import com.bill.portal.scrm.core.annotation.ConditionalOnSuiteMode;
import com.bill.portal.scrm.core.annotation.ConditionalOnWxMode;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import java.util.Map;

/**
 * 企微模式加载条件判断
 */
public class WxModeCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String expectedMode = null;

        // 兼容通用注解 @ConditionalOnWxMode
        if (metadata.isAnnotated(ConditionalOnWxMode.class.getName())) {
            Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnWxMode.class.getName());
            expectedMode = (String) attributes.get("value");
        }
        // 兼容语法糖注解 @ConditionalOnSelfMode
        else if (metadata.isAnnotated(ConditionalOnSelfMode.class.getName())) {
            expectedMode = "SELF";
        }
        // 兼容语法糖注解 @ConditionalOnSuiteMode
        else if (metadata.isAnnotated(ConditionalOnSuiteMode.class.getName())) {
            expectedMode = "SUITE";
        }

        if (expectedMode == null) return false;

        // 从环境变量中读取当前模式，默认为 SELF
        String currentMode = context.getEnvironment().getProperty("xqt.wx.mode", "SELF");
        return expectedMode.equalsIgnoreCase(currentMode);
    }

}
