package com.bill.portal.common.core.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.bill.portal.common.core.datascope.DataScopeHandler;
import com.bill.portal.common.core.datascope.DataScopeProperties;
import com.bill.portal.common.core.datascope.DataScopeRouter;
import com.bill.portal.common.core.datascope.DataScopeScopeResolver;
import com.bill.portal.common.core.tenant.TenantConfigProperties;
import com.bill.portal.common.core.tenant.TenantHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mybatis Plus Config
 */
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(TenantLineInnerInterceptor tenantLineInnerInterceptor,
                                                         DataPermissionInterceptor dataPermissionInterceptor) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 注入多租户支持
        interceptor.addInnerInterceptor(tenantLineInnerInterceptor);
        // 数据权限（各模块实现 DataScopeHandler 按表生效，无 Handler 的模块直通）
        interceptor.addInnerInterceptor(dataPermissionInterceptor);
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    /**
     * 创建租户维护处理器对象
     * @return 处理后的租户维护处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public TenantLineInnerInterceptor tenantLineInnerInterceptor(TenantConfigProperties tenantConfigProperties) {
        TenantLineInnerInterceptor tenantLineInnerInterceptor = new TenantLineInnerInterceptor();
        tenantLineInnerInterceptor.setTenantLineHandler(new TenantHandler(tenantConfigProperties));
        return tenantLineInnerInterceptor;
    }

    /**
     * 数据权限拦截器：按表路由到各模块的 DataScopeHandler（见 datascope 包）。
     * 顺序：租户 → 数据权限 → 分页（先拼权限条件再 count）
     */
    @Bean
    @ConditionalOnMissingBean
    public DataPermissionInterceptor dataPermissionInterceptor(ObjectProvider<DataScopeHandler> handlerProvider,
                                                               ObjectProvider<DataScopeScopeResolver> resolverProvider,
                                                               DataScopeProperties properties) {
        // Handler/解析器由 Router 懒解析（容器刷新期实例化 Mapper 会与 sqlSessionTemplate 循环依赖）
        return new DataPermissionInterceptor(
                new DataScopeRouter(handlerProvider, resolverProvider, properties));
    }

}
