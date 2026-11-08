package com.bill.portal.common.core.datascope;

import lombok.Getter;

import java.io.Serializable;
import java.util.List;

/**
 * 数据权限范围视图：一个登录用户经角色 data_scope 反推出的"可见系统账号集合"。
 * - ALL  = 不限制
 * - DENY = 无角色/无可见范围，业务侧拼恒假条件
 * - USERS= 可见的系统账号ID集合（业务侧再映射为自己的数据归属）
 * 由数据范围解析器（{@link DataScopeScopeResolver}）产出，存于 {@link DataScopeContextHolder}。
 */
@Getter
public class DataScopeView implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Mode { ALL, DENY, USERS }

    private final Mode mode;

    /** mode=USERS 时的可见系统账号ID集合 */
    private final List<Long> userIds;

    private DataScopeView(Mode mode, List<Long> userIds) {
        this.mode = mode;
        this.userIds = userIds;
    }

    public static DataScopeView all() {
        return new DataScopeView(Mode.ALL, List.of());
    }

    public static DataScopeView deny() {
        return new DataScopeView(Mode.DENY, List.of());
    }

    public static DataScopeView ofUsers(List<Long> userIds) {
        return userIds == null || userIds.isEmpty()
                ? deny()
                : new DataScopeView(Mode.USERS, List.copyOf(userIds));
    }
}
