package com.bill.portal.common.core.client;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.experimental.UtilityClass;


/**
 * 客户端工具类
 */
@UtilityClass
public class ClientContextHolder {

	private final ThreadLocal<String> THREAD_LOCAL_CLIENT = new TransmittableThreadLocal<>();

	/**
	 * TTL 设置客户端ID
	 * @param clientId 客户端标识
	 */
	public void setClientId(String clientId) {
		THREAD_LOCAL_CLIENT.set(clientId);
	}

	/**
	 * 获取TTL中的客户端ID
	 * @return 客户端标识
	 */
	public String getClientId() {
		return THREAD_LOCAL_CLIENT.get();
	}

	public void clear() {
		THREAD_LOCAL_CLIENT.remove();
	}

}
