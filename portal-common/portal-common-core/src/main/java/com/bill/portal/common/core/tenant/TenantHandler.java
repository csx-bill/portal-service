package com.bill.portal.common.core.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;

/**
 * 租户维护处理器
 */
@Slf4j
@RequiredArgsConstructor
public class TenantHandler implements TenantLineHandler {

	private final TenantConfigProperties properties;

	/**
	 * 获取租户 ID 值表达式，只支持单个 ID 值
	 * <p>
	 * @return 租户 ID 值表达式
	 */
	@Override
	public Expression getTenantId() {
		Long tenantId = TenantContextHolder.getTenantId();
		log.debug("当前租户为 >> {}", tenantId);

		if (tenantId == null) {
			return new NullValue();
		}
		return new LongValue(tenantId);
	}

	/**
	 * 获取租户字段名
	 * @return 租户字段名
	 */
	@Override
	public String getTenantIdColumn() {
		return properties.getColumn();
	}

	/**
	 * 根据表名判断是否忽略拼接多租户条件
	 * <p>
	 * 默认都要进行解析并拼接多租户条件
	 * @param tableName 表名
	 * @return 是否忽略, true:表示忽略，false:需要解析并拼接多租户条件
	 */
	@Override
	public boolean ignoreTable(String tableName) {
		// 判断是否跳过当前查询的租户过滤
		if (TenantContextHolder.getTenantSkip()) {
			return Boolean.TRUE;
		}

		Long tenantId = TenantContextHolder.getTenantId();
		// 租户中ID 为空，查询全部，不进行过滤
		if (tenantId == null) {
			return Boolean.TRUE;
		}

		return !properties.getTables().contains(tableName);
	}

}
