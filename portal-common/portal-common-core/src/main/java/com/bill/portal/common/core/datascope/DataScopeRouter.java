package com.bill.portal.common.core.datascope;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

/**
 * 数据权限路由器：按表名轮询各模块的 {@link DataScopeHandler}，
 * 第一个 supports 该表的 Handler 出条件；无人认领 → 不拼（模块不参与数据权限）。
 *
 * Handler/范围解析器均通过 ObjectProvider 注入并在【首次使用时】才解析：
 * 数据权限拦截器 Bean 的创建发生在 MybatisPlusAutoConfiguration 构造期间，
 * 提前实例化依赖 Mapper 的模块 Bean 会造成与 sqlSessionTemplate 的循环依赖。
 * Handler 构造异常时返回恒假条件（拒绝而非放行，避免越权）。
 */
@Slf4j
@RequiredArgsConstructor
public class DataScopeRouter implements MultiDataPermissionHandler {

    private final ObjectProvider<DataScopeHandler> handlerProvider;

    private final ObjectProvider<DataScopeScopeResolver> resolverProvider;

    private final DataScopeProperties properties;

    /** 懒解析缓存（首次使用后不再变化；Bean 定义在容器刷新期内不会变） */
    private volatile List<DataScopeHandler> handlers;

    private volatile DataScopeScopeResolver resolver;

    /** 解析进行中标志：防止解析自身的嵌套查询再次触发解析（无限递归） */
    private final ThreadLocal<Boolean> RESOLVING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /** 已告警过的同表多 Handler 冲突（避免日志刷屏） */
    private final Set<String> warnedConflicts = ConcurrentHashMap.newKeySet();

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        init();
        if (!properties.isEnabled() || handlers.isEmpty() || DataScopeContextHolder.getSkip()) {
            return null;
        }
        resolveScopeOnce();
        String tableName = table.getName();
        DataScopeHandler matched = null;
        String matchedHandler = null;
        for (DataScopeHandler handler : handlers) {
            if (!handler.supports(tableName)) {
                continue;
            }
            if (matched == null) {
                matched = handler;
                matchedHandler = handler.getClass().getSimpleName();
            } else if (warnedConflicts.add(tableName.toLowerCase())) {
                // 同表被多个 Handler 声明：首命中者生效，其余忽略（每表仅告警一次）
                log.warn("表 {} 同时被多个 DataScopeHandler 支持({} 与 {}), 仅 {} 生效",
                        tableName, matchedHandler, handler.getClass().getSimpleName(), matchedHandler);
            }
        }
        if (matched == null) {
            return null;
        }
        try {
            return matched.buildCondition(table);
        } catch (Exception e) {
            log.error("数据权限条件构造失败, 按拒绝处理. table={}, msId={}", tableName, mappedStatementId, e);
            return deny();
        }
    }

    /** 首次使用时解析 Handler 列表与范围解析器（此时容器已刷新完毕，无循环依赖） */
    private void init() {
        if (this.handlers != null) {
            return;
        }
        synchronized (this) {
            if (this.handlers == null) {
                this.handlers = handlerProvider.orderedStream().toList();
                this.resolver = resolverProvider.getIfAvailable();
            }
        }
    }

    /** 每请求首次命中时解析可见范围视图并缓存；解析进行中的嵌套查询直接跳过（防递归） */
    private void resolveScopeOnce() {
        if (DataScopeContextHolder.getScope() != null || RESOLVING.get()) {
            return;
        }
        Long userId = DataScopeContextHolder.getUserId();
        if (userId == null || resolver == null) {
            return;
        }
        RESOLVING.set(Boolean.TRUE);
        try {
            DataScopeContextHolder.setScope(resolver.resolveScope(userId));
        } catch (Exception e) {
            log.error("数据权限范围解析失败, 按拒绝处理. userId={}", userId, e);
            DataScopeContextHolder.setScope(DataScopeView.deny());
        } finally {
            RESOLVING.remove();
        }
    }

    /** 恒假条件 1 = 0：数据权限计算失败时拒绝而非放行 */
    private Expression deny() {
        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new LongValue(1));
        equalsTo.setRightExpression(new LongValue(0));
        return equalsTo;
    }
}
