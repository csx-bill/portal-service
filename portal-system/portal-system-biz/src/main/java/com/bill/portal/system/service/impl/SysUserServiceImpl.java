package com.bill.portal.system.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bill.portal.common.core.exception.CheckedException;
import com.bill.portal.common.core.security.UserInfo;
import com.bill.portal.system.dto.UserDTO;
import com.bill.portal.system.entity.SysDept;
import com.bill.portal.system.entity.SysUser;
import com.bill.portal.system.entity.SysUserDept;
import com.bill.portal.system.entity.SysUserRole;
import com.bill.portal.system.mapper.SysDeptMapper;
import com.bill.portal.system.mapper.SysUserDeptMapper;
import com.bill.portal.system.mapper.SysUserMapper;
import com.bill.portal.system.mapper.SysUserRoleMapper;
import com.bill.portal.system.service.ISysUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysUserDeptMapper sysUserDeptMapper;

    private final SysUserRoleMapper sysUserRoleMapper;

    private final SysDeptMapper sysDeptMapper;

    @Override
    public UserInfo getUserInfo(UserDTO query) {
        SysUser sysUser = baseMapper.selectOne(
                Wrappers.<SysUser>lambdaQuery()
                        .eq(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                        .eq(StringUtils.hasText(query.getMobile()), SysUser::getMobile, query.getMobile())
        );
        UserInfo userInfo = BeanUtil.copyProperties(sysUser, UserInfo.class);
        return userInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUser(SysUser entity) {
        if (StringUtils.hasText(entity.getPassword())) {
            entity.setPassword(BCrypt.hashpw(entity.getPassword(), BCrypt.gensalt()));
        }
        super.save(entity);
        saveUserDepts(entity.getId(), entity.getDeptIds());
        saveUserRoles(entity.getId(), entity.getRoleIds());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser entity) {
        // 密码留空则不修改（null 字段 MP 更新时自动跳过）
        if (StringUtils.hasText(entity.getPassword())) {
            entity.setPassword(BCrypt.hashpw(entity.getPassword(), BCrypt.gensalt()));
        } else {
            entity.setPassword(null);
        }
        super.updateById(entity);
        // 编辑表单不携带部门字段时保持原有关联，仅在显式传入时重建
        if (entity.getDeptIds() != null) {
            sysUserDeptMapper.delete(new LambdaQueryWrapper<SysUserDept>()
                    .eq(SysUserDept::getUserId, entity.getId()));
            saveUserDepts(entity.getId(), entity.getDeptIds());
        }
        // 角色同理：显式传入时整组重建（未传 = 不调整）
        if (entity.getRoleIds() != null) {
            sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, entity.getId()));
            saveUserRoles(entity.getId(), entity.getRoleIds());
        }
        return true;
    }

    @Override
    public boolean updateProfile(Long userId, SysUser profile) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new CheckedException("用户不存在");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setName(profile.getName());
        update.setMobile(profile.getMobile());
        update.setEmail(profile.getEmail());
        update.setGender(profile.getGender());
        // 用户名是登录标识，允许自助修改但不可与他人冲突；未变更时不触碰
        if (StringUtils.hasText(profile.getUsername())
                && !profile.getUsername().equals(user.getUsername())) {
            long exists = count(Wrappers.<SysUser>lambdaQuery()
                    .eq(SysUser::getUsername, profile.getUsername()));
            if (exists > 0) {
                throw new CheckedException("用户名已被占用");
            }
            update.setUsername(profile.getUsername());
        }
        return updateById(update);
    }

    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new CheckedException("用户不存在");
        }
        // 校验旧密码（存量用户密码列为空时视为不匹配，需管理员重置）
        if (!StringUtils.hasText(user.getPassword())
                || !BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new CheckedException("旧密码不正确");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        return updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeUserById(Serializable id) {
        sysUserDeptMapper.delete(new LambdaQueryWrapper<SysUserDept>()
                .eq(SysUserDept::getUserId, id));
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));
        return super.removeById(id);
    }

    @Override
    public SysUser getUserDetail(Serializable id) {
        SysUser user = super.getById(id);
        if (user != null) {
            fillDepts(List.of(user));
            fillRoles(List.of(user));
            maskCredentials(user);
        }
        return user;
    }

    @Override
    public <E extends IPage<SysUser>> E pageUsers(E page, Wrapper<SysUser> wrapper) {
        E result = super.page(page, wrapper);
        fillDepts(result.getRecords());
        // 凭据哈希不下发前端：避免整行透传更新时哈希被二次加密
        result.getRecords().forEach(this::maskCredentials);
        return result;
    }

    private void maskCredentials(SysUser user) {
        user.setPassword(null);
        user.setSalt(null);
    }

    @Override
    public List<Long> listUserIdsByDeptIds(List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return List.of();
        }
        return sysUserDeptMapper.selectList(new LambdaQueryWrapper<SysUserDept>()
                        .in(SysUserDept::getDeptId, deptIds))
                .stream()
                .map(SysUserDept::getUserId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 批量回填用户的所属部门 ID 与名称
     */
    private void fillDepts(List<SysUser> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        List<Long> userIds = users.stream().map(SysUser::getId).collect(Collectors.toList());
        List<SysUserDept> relations = sysUserDeptMapper.selectList(
                new LambdaQueryWrapper<SysUserDept>().in(SysUserDept::getUserId, userIds));
        if (relations.isEmpty()) {
            return;
        }
        List<Long> deptIds = relations.stream().map(SysUserDept::getDeptId).distinct()
                .collect(Collectors.toList());
        Map<Long, String> nameMap = sysDeptMapper.selectBatchIds(deptIds).stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getName));
        Map<Long, List<Long>> deptIdsByUser = relations.stream()
                .collect(Collectors.groupingBy(SysUserDept::getUserId,
                        Collectors.mapping(SysUserDept::getDeptId, Collectors.toList())));
        for (SysUser user : users) {
            List<Long> ids = deptIdsByUser.getOrDefault(user.getId(), List.of());
            user.setDeptIds(ids);
            user.setDeptNames(ids.stream()
                    .map(nameMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
    }

    /**
     * 保存用户-部门关联
     */
    private void saveUserDepts(Long userId, List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) return;
        for (Long deptId : deptIds) {
            SysUserDept ud = new SysUserDept();
            ud.setUserId(userId);
            ud.setDeptId(deptId);
            sysUserDeptMapper.insert(ud);
        }
    }

    /**
     * 保存用户-角色关联
     */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return;
        for (Long roleId : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            ur.setCreateTime(java.time.LocalDateTime.now());
            sysUserRoleMapper.insert(ur);
        }
    }

    /**
     * 批量回填用户的角色 ID 列表
     */
    private void fillRoles(List<SysUser> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        List<Long> userIds = users.stream().map(SysUser::getId).collect(Collectors.toList());
        Map<Long, List<Long>> roleIdsByUser = sysUserRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds))
                .stream()
                .collect(Collectors.groupingBy(SysUserRole::getUserId,
                        Collectors.mapping(SysUserRole::getRoleId, Collectors.toList())));
        for (SysUser user : users) {
            user.setRoleIds(roleIdsByUser.getOrDefault(user.getId(), List.of()));
        }
    }
}
