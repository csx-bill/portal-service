package com.bill.portal.common.core.datascope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限归属声明：标注在业务实体上，声明该表的归属列
 * （数据权限按归属列过滤，如 wx_customer 的跟进人 user_id）。
 * 各模块的 {@link DataScopeHandler} 经 TableInfoHelper 运行时解析注解，
 * 模块内加表只需在实体上标注，无需改 Handler 代码。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScopeOwner {

    /** 归属列名（如 user_id / dept_id / create_by） */
    String column();
}
