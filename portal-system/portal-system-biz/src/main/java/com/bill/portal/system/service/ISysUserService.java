package com.bill.portal.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bill.portal.common.core.security.UserInfo;
import com.bill.portal.system.dto.UserDTO;
import com.bill.portal.system.entity.SysUser;

import java.io.Serializable;
import java.util.List;

public interface ISysUserService extends IService<SysUser> {

    UserInfo getUserInfo(UserDTO query);

    /**
     * 新增用户：密码 BCrypt 加密 + 保存用户-部门关联
     */
    boolean saveUser(SysUser entity);

    /**
     * 更新用户：密码留空则不修改；显式传入 deptIds 时才重建用户-部门关联
     */
    boolean updateUser(SysUser entity);

    /**
     * 更新个人资料：当前登录用户自助修改基本资料
     * （姓名/用户名/手机号/邮箱/性别；username 变更时查重）
     */
    boolean updateProfile(Long userId, SysUser profile);

    /**
     * 修改密码：校验旧密码，新密码 BCrypt 加密（当前登录用户自助）
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 删除用户并清理用户-部门关联
     */
    boolean removeUserById(Serializable id);

    /**
     * 用户详情（回填 deptIds / deptNames）
     */
    SysUser getUserDetail(Serializable id);

    /**
     * 分页查询并回填每行的 deptIds / deptNames
     */
    <E extends IPage<SysUser>> E pageUsers(E page, Wrapper<SysUser> wrapper);

    /**
     * 按部门 ID 集合查询关联的用户 ID 列表（去重）
     */
    List<Long> listUserIdsByDeptIds(List<Long> deptIds);
}
