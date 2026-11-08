package com.bill.portal.common.core.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import com.bill.portal.common.core.client.ClientInterceptor;
import com.bill.portal.common.core.datascope.DataScopeInterceptor;
import com.bill.portal.common.core.security.SecurityProperties;
import com.bill.portal.common.core.tenant.TenantInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final SecurityProperties securityProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 免认证地址（security.ignore-urls）统一跳过所有拦截器
        List<String> ignoreUrls = securityProperties.getIgnoreUrls();
        registry.addInterceptor(new TenantInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(ignoreUrls);
        registry.addInterceptor(new ClientInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(ignoreUrls);
        registry.addInterceptor(new DataScopeInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(ignoreUrls);
        // Sa-Token 注解校验（@SaCheckPermission / @SaIgnore 等），
        // 放在租户/客户端拦截器之后，权限查询时上下文已就绪
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(ignoreUrls);
    }
}
