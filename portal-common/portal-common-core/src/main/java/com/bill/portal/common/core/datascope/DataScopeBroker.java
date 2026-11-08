package com.bill.portal.common.core.datascope;

import lombok.experimental.UtilityClass;

/**
 * 数据权限运行时代理：供回调、定时 Job 等无登录上下文的系统级操作使用。
 *
 * 这类线程没有登录用户，数据权限路由器查不到 userId 时各模块 Handler
 * 按"拒绝"拼恒假条件（fail-closed，防越权），系统级查询会被过滤为空。
 * 用本类包住处理逻辑即显式全量放行，结束后恢复外层标识（支持嵌套），
 * 与 {@link com.bill.portal.common.core.tenant.TenantBroker} 配套使用：
 * <pre>
 *     TenantBroker.runAs(tenantId, ignored -&gt;
 *             DataScopeBroker.skip(() -&gt; syncService.sync(...)));
 * </pre>
 */
@UtilityClass
public class DataScopeBroker {

	@FunctionalInterface
	public interface SkipRun {

		/**
		 * 执行业务逻辑
		 * @throws Exception
		 */
		void run() throws Exception;

	}

	@FunctionalInterface
	public interface SkipApply<T> {

		/**
		 * 执行业务逻辑，返回一个值
		 * @return
		 * @throws Exception
		 */
		T get() throws Exception;

	}

	/**
	 * 跳过数据权限运行（无返回值）
	 */
	public void skip(SkipRun func) {
		final Boolean pre = DataScopeContextHolder.getSkip();
		DataScopeContextHolder.setSkip();
		try {
			func.run();
		}
		catch (Exception e) {
			throw new DataScopeBrokerExceptionWrapper(e.getMessage(), e);
		}
		finally {
			restoreSkip(pre);
		}
	}

	/**
	 * 跳过数据权限运行（有返回值）
	 */
	public <T> T skipAs(SkipApply<T> func) {
		final Boolean pre = DataScopeContextHolder.getSkip();
		DataScopeContextHolder.setSkip();
		try {
			return func.get();
		}
		catch (Exception e) {
			throw new DataScopeBrokerExceptionWrapper(e.getMessage(), e);
		}
		finally {
			restoreSkip(pre);
		}
	}

	private void restoreSkip(Boolean pre) {
		if (Boolean.TRUE.equals(pre)) {
			DataScopeContextHolder.setSkip();
		}
		else {
			DataScopeContextHolder.clearSkip();
		}
	}

	public static class DataScopeBrokerExceptionWrapper extends RuntimeException {

		public DataScopeBrokerExceptionWrapper(String message, Throwable cause) {
			super(message, cause);
		}

		public DataScopeBrokerExceptionWrapper(Throwable cause) {
			super(cause);
		}

	}

}
