package com.bill.portal.system.service.impl;

import cn.dev33.satoken.oauth2.consts.GrantType;
import cn.hutool.core.bean.BeanUtil;
import com.bill.portal.common.core.security.IPermissionService;
import com.bill.portal.common.core.security.IUserDetailsService;
import com.bill.portal.common.core.security.UserInfo;
import com.bill.portal.system.dto.UserDTO;
import com.bill.portal.system.entity.SysUser;
import com.bill.portal.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements IUserDetailsService {

    private final ISysUserService sysUserService;

    private final IPermissionService permissionService;

    @Override
    public boolean support(String clientId, String grantType) {
        return GrantType.password.equals(grantType);
    }

    @Override
    public UserInfo loadUserByUsername(String username) {
        UserDTO query = new UserDTO();
        query.setUsername(username);
        return sysUserService.getUserInfo(query);
    }

    @Override
    public UserInfo getUserDetails(Object userId) {
        SysUser sysUser = sysUserService.getById(Long.valueOf(userId.toString()));
        if (sysUser == null) {
            return null;
        }
        UserInfo userInfo = BeanUtil.copyProperties(sysUser, UserInfo.class);
        userInfo.setPassword(null);
        // 权限码按数据库配置原样下发（全量格式 clientId:action），前端按钮显隐据此比对
        userInfo.setPermissions(permissionService.getPermissionList(userInfo.getId()));
        return userInfo;
    }
}
