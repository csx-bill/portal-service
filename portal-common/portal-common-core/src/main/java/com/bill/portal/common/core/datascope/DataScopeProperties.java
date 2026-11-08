package com.bill.portal.common.core.datascope;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 数据权限配置（data-scope.*）。
 * 各模块的过滤逻辑由本模块的 {@link DataScopeHandler} Bean 决定
 * （Bean 存在即生效）；此处仅提供全局开关。
 */
@Data
@Component
@ConfigurationProperties(prefix = "data-scope")
public class DataScopeProperties {

    /** 数据权限总开关（默认开启；无任何 Handler Bean 时拦截器等价于直通） */
    private boolean enabled = true;
}
