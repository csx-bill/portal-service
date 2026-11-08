package com.bill.portal.common.core.jackson;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.ser.std.ToStringSerializer;

/**
 * Long 类型全局序列化模块
 */
public class LongModule extends SimpleModule {

    /**
     * 构造模块并注册 Long ↔ JSON 双向规则。
     * <p>
     * 序列化：Long/long → 字符串（如雪花 ID 字段，防止前端精度丢失）；
     * 反序列化：字符串/数字 → Long（前端回传的雪花 ID 为字符串形式，
     * Jackson 3 默认禁止 String → Long 宽松转换，需显式注册）。
     */
    public LongModule() {
        super("LongModule");

        // Long 包装类型 → 字符串（如雪花 ID 字段）
        this.addSerializer(Long.class, ToStringSerializer.instance);

        // long 基本类型 → 字符串（如 long 类型的 count/timestamp 等）
        this.addSerializer(Long.TYPE, ToStringSerializer.instance);

        // 字符串/数字 → Long（雪花 ID 回传）
        this.addDeserializer(Long.class, new FlexibleLongDeserializer());
        this.addDeserializer(Long.TYPE, new FlexibleLongDeserializer());
    }

    /**
     * 宽松 Long 反序列化：兼容字符串与数字两种形式，JSON null 返回 null
     */
    public static class FlexibleLongDeserializer extends StdDeserializer<Long> {

        protected FlexibleLongDeserializer() {
            super(Long.class);
        }

        @Override
        public Long deserialize(JsonParser p, DeserializationContext ctxt) {
            if (p.currentToken() == JsonToken.VALUE_NULL) {
                return null;
            }
            return p.getValueAsLong();
        }
    }
}
