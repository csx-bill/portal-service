package com.bill.portal.common.core.jackson;

import cn.hutool.core.date.DatePattern;
import tools.jackson.core.json.PackageVersion;
import tools.jackson.databind.ext.javatime.deser.*;
import tools.jackson.databind.ext.javatime.ser.*;
import tools.jackson.databind.module.SimpleModule;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Java 8 时间类型全局序列化模块
 * <p>
 * 继承 {@link SimpleModule}，统一注册 Java 8 时间 API 的序列化器与反序列化器，
 * 确保前后端交互时时间格式的一致性。
 * <p>
 * 覆盖的类型：
 * <ul>
 *   <li>{@link LocalDateTime}   → yyyy-MM-dd HH:mm:ss</li>
 *   <li>{@link LocalDate}       → yyyy-MM-dd</li>
 *   <li>{@link LocalTime}       → HH:mm:ss</li>
 *   <li>{@link Instant}         → 时间戳数值</li>
 *   <li>{@link Duration}        → 毫秒数值</li>
 * </ul>
 * <p>
 * 使用方式：通过 {@code JsonMapper.Builder.addModule(new JavaTimeModule())} 注册。
 *
 * @see LongModule
 */
public class JavaTimeModule extends SimpleModule {

    /**
     * 构造模块并注册 Java 8 时间类型的序列化 / 反序列化规则。
     * <p>
     * 序列化器负责 Java 对象 → JSON 字符串，
     * 反序列化器负责 JSON 字符串 → Java 对象。
     */
    public JavaTimeModule() {
        super(PackageVersion.VERSION);

        // ======================= 时间序列化规则 ===============================
        // yyyy-MM-dd HH:mm:ss
        this.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
        // yyyy-MM-dd
        this.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
        // HH:mm:ss
        this.addSerializer(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(DatePattern.NORM_TIME_PATTERN)));

        // Instant 类型序列化
        this.addSerializer(Instant.class, InstantSerializer.INSTANCE);

        // Duration 类型序列化
        this.addSerializer(Duration.class, DurationSerializer.INSTANCE);

        // ======================= 时间反序列化规则 ==============================
        // yyyy-MM-dd HH:mm:ss
        this.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));
        // yyyy-MM-dd
        this.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(DatePattern.NORM_DATE_PATTERN)));
        // HH:mm:ss
        this.addDeserializer(LocalTime.class,
                new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DatePattern.NORM_TIME_PATTERN)));
        // Instant 反序列化
        this.addDeserializer(Instant.class, InstantDeserializer.INSTANT);

        // Duration 反序列化
        this.addDeserializer(Duration.class, DurationDeserializer.INSTANCE);
    }
}
