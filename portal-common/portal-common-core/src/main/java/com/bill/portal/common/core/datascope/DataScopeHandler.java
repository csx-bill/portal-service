package com.bill.portal.common.core.datascope;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;

/**
 * 数据权限 SPI：各业务模块实现本接口，声明自己管辖的表与过滤条件构造逻辑。
 * 条件形状不限（成员集合 IN / 部门 IN / 等值……由模块自定义）；
 * 未实现 Handler 的模块天然不参与数据权限过滤。
 *
 * 对 Wrapper 生成与 XML 手写的 SQL 均生效（拦截在执行层按 JSqlParser
 * 语法树逐表回调）；JOIN/子查询中的表按 Table（含别名）分别回调。
 *
 * 表归属：各模块 Handler 只声明本模块的表，同表冲突按 Bean 顺序
 * 首命中生效（顺序可用 @Order 调整）。
 */
public interface DataScopeHandler {

    /** 该表是否归本模块管（表名不区分大小写） */
    boolean supports(String tableName);

    /**
     * 构造追加到 WHERE 的条件。
     * @param table 命中的表（含别名，条件列需按别名限定避免多表歧义）
     * @return 追加条件；返回 null 表示该表不拼权限条件
     */
    Expression buildCondition(Table table);
}
