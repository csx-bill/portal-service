package com.bill.portal.common.core.config;

import com.bill.portal.common.core.jackson.JavaTimeModule;
import com.bill.portal.common.core.jackson.LongModule;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;

/**
 * Jackson 全局序列化配置
 *
 * <p>
 * 通过 {@link JsonMapperBuilderCustomizer} 向自动配置的 {@code JsonMapper}
 * 注册自定义模块，实现以下全局序列化规则：
 * <ul>
 *   <li><b>Java 8 时间类型</b> — 统一格式化（见 {@link JavaTimeModule}）</li>
 *   <li><b>Long 类型</b> — 序列化为字符串，防止前端精度丢失（见 {@link LongModule}）</li>
 * </ul>
 *
 * @see JavaTimeModule
 * @see LongModule
 */
@Configuration
public class JacksonConfig {

    /**
     * 注册全局 Jackson 自定义模块。
     * <p>
     * 当前注册的模块：
     * <ul>
     *   <li>{@link JavaTimeModule} — Java 8 时间类型序列化规则</li>
     *   <li>{@link LongModule} — Long → String 序列化规则</li>
     * </ul>
     * <p>
     * 如需新增其他全局序列化规则，参照已有模块创建新的 {@code SimpleModule} 子类，
     * 在此方法中追加即可。
     */
    @Bean
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer() {
        return builder -> {
            builder.addModules(new JavaTimeModule(), new LongModule()).enable(SerializationFeature.INDENT_OUTPUT)
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        };
    }

}
