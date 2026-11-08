package com.bill.portal.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.common.core.model.R;
import com.bill.portal.common.core.security.IUserDetailsService;
import com.bill.portal.common.core.security.UserInfo;
import com.bill.portal.system.dto.SysUserCreateReq;
import com.bill.portal.system.dto.SysUserPasswordReq;
import com.bill.portal.system.dto.SysUserProfileReq;
import com.bill.portal.system.dto.SysUserQuery;
import com.bill.portal.system.dto.SysUserUpdateReq;
import com.bill.portal.system.entity.SysUser;
import com.bill.portal.system.service.ISysDeptService;
import com.bill.portal.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/user")
@Tag(name = "系统用户", description = "系统用户接口")
@RequiredArgsConstructor
public class SysUserController {

    private final ISysUserService sysUserService;

    private final ISysDeptService sysDeptService;

    private final IUserDetailsService userDetailsService;

    @GetMapping("/page")
    @Operation(summary = "分页查询", description = "分页查询")
    public R<IPage<SysUser>> page(@ParameterObject Page<SysUser> page,
                                   @ParameterObject SysUserQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.<SysUser>lambdaQuery()
                .like(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .like(StringUtils.hasText(query.getName()), SysUser::getName, query.getName())
                .like(StringUtils.hasText(query.getMobile()), SysUser::getMobile, query.getMobile())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus());
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(SysUser::getUsername, query.getKeyword())
                    .or()
                    .like(SysUser::getName, query.getKeyword())
                    .or()
                    .like(SysUser::getMobile, query.getKeyword()));
        }
        // 按部门过滤：部门及其子部门下的用户
        if (query.getDeptId() != null) {
            List<Long> deptIds = sysDeptService.listSelfAndChildIds(query.getDeptId());
            List<Long> userIds = sysUserService.listUserIdsByDeptIds(deptIds);
            if (userIds.isEmpty()) {
                return R.success(new Page<>(page.getCurrent(), page.getSize()));
            }
            wrapper.in(SysUser::getId, userIds);
        }
        return R.success(sysUserService.pageUsers(page, wrapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "通过id查询", description = "通过id查询")
    public R<SysUser> getById(@PathVariable("id") Long id) {
        return R.success(sysUserService.getUserDetail(id));
    }

    @PostMapping
    @SaCheckPermission("#clientId:user:add")
    @Operation(summary = "新增", description = "新增")
    public R<Boolean> save(@RequestBody SysUserCreateReq req) {
        SysUser entity = BeanUtil.copyProperties(req, SysUser.class);
        return R.success(sysUserService.saveUser(entity));
    }

    @PutMapping
    @SaCheckPermission("#clientId:user:update")
    @Operation(summary = "更新", description = "更新")
    public R<Boolean> updateById(@RequestBody SysUserUpdateReq req) {
        SysUser entity = BeanUtil.copyProperties(req, SysUser.class);
        return R.success(sysUserService.updateUser(entity));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("#clientId:user:delete")
    @Operation(summary = "删除", description = "删除")
    public R<Boolean> removeById(@PathVariable("id") Long id) {
        return R.success(sysUserService.removeUserById(id));
    }

    @DeleteMapping
    @SaCheckPermission("#clientId:user:delete")
    @Operation(summary = "批量删除", description = "批量删除")
    public R<Boolean> removeBatchByIds(@RequestParam("ids") String ids) {
        for (String id : ids.split(",")) {
            sysUserService.removeUserById(Long.valueOf(id));
        }
        return R.success(true);
    }

    /**
     * 获取指定用户全部信息
     * @return 用户信息
     */
    @SaIgnore
    @GetMapping("/info/{username}")
    public R<UserInfo> info(@PathVariable("username") String username) {
        return R.success(userDetailsService.loadUserByUsername(username));
    }

    @GetMapping("/info")
    @Operation(summary = "用户信息")
    public R<?> info() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserInfo userInfo = userDetailsService.getUserDetails(userId);
        return R.success(userInfo);
    }

    /**
     * 当前登录用户自助更新个人资料（姓名/用户名）。
     * 与用户管理接口的 user:update 权限无关——本人资料本人可改。
     */
    @PutMapping("/profile")
    @Operation(summary = "更新个人资料", description = "当前登录用户自助更新姓名/用户名")
    public R<Boolean> updateProfile(@RequestBody @Valid SysUserProfileReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser entity = new SysUser();
        entity.setName(req.getName());
        entity.setUsername(req.getUsername());
        entity.setMobile(req.getMobile());
        entity.setEmail(req.getEmail());
        entity.setGender(req.getGender());
        return R.success(sysUserService.updateProfile(userId, entity));
    }

    /**
     * 当前登录用户自助修改密码（校验旧密码）。
     */
    @PutMapping("/profile/password")
    @Operation(summary = "修改密码", description = "当前登录用户自助修改密码")
    public R<Boolean> updatePassword(@RequestBody @Valid SysUserPasswordReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.success(sysUserService.updatePassword(
                userId, req.getOldPassword(), req.getNewPassword()));
    }
}
