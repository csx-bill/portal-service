package com.bill.portal.common.core.datascope;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.experimental.UtilityClass;

/**
 * 数据权限上下文：当前登录用户ID（由 DataScopeInterceptor 从令牌解析）、
 * 可见范围视图（由 DataScopeScopeResolver 解析、每请求首用时缓存）
 * 与放行开关（同步 Job / 回调等无登录上下文的全量操作使用）。
 */
@UtilityClass
public class DataScopeContextHolder {

    private final ThreadLocal<Long> THREAD_LOCAL_USER = new TransmittableThreadLocal<>();

    private final ThreadLocal<DataScopeView> THREAD_LOCAL_SCOPE = new TransmittableThreadLocal<>();

    private final ThreadLocal<Boolean> THREAD_LOCAL_SKIP_FLAG = new TransmittableThreadLocal<>();

    public void setUserId(Long userId) {
        THREAD_LOCAL_USER.set(userId);
    }

    public Long getUserId() {
        return THREAD_LOCAL_USER.get();
    }

    /** 设置可见范围视图（每请求首次解析后缓存） */
    public void setScope(DataScopeView scope) {
        THREAD_LOCAL_SCOPE.set(scope);
    }

    /** 读取可见范围视图；未解析时为 null */
    public DataScopeView getScope() {
        return THREAD_LOCAL_SCOPE.get();
    }

    /** 设置放行标识（本次线程内数据权限全部跳过） */
    public void setSkip() {
        THREAD_LOCAL_SKIP_FLAG.set(Boolean.TRUE);
    }

    public Boolean getSkip() {
        return THREAD_LOCAL_SKIP_FLAG.get() != null && THREAD_LOCAL_SKIP_FLAG.get();
    }

    /** 仅清除放行标识（供嵌套场景恢复外层状态，不影响 userId/scope） */
    public void clearSkip() {
        THREAD_LOCAL_SKIP_FLAG.remove();
    }

    public void clear() {
        THREAD_LOCAL_USER.remove();
        THREAD_LOCAL_SCOPE.remove();
        THREAD_LOCAL_SKIP_FLAG.remove();
    }
}
