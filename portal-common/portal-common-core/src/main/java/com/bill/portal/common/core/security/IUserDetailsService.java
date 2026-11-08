package com.bill.portal.common.core.security;


public interface IUserDetailsService {
    /**
     * 是否支持此客户端校验
     * @param clientId 目标客户端
     * @return true/false
     */
    default boolean support(String clientId, String grantType) {
        return true;
    }

    /**
     * 根据用户名加载用户信息的方法接口
     * @param username 用户名
     * @return 用户ID，或 null（认证失败）
     */
    UserInfo loadUserByUsername(String username);

    /**
     * 获取用户详情
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfo getUserDetails(Object userId);
}
