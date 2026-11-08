package com.bill.portal.system.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import com.bill.portal.common.core.client.ClientContextHolder;
import com.bill.portal.common.core.model.R;
import com.bill.portal.system.dto.SysMenuCreateReq;
import com.bill.portal.system.dto.SysMenuUpdateReq;
import com.bill.portal.system.entity.SysMenu;
import com.bill.portal.system.service.ISysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/menu")
@Tag(name = "系统菜单", description = "系统菜单接口")
@RequiredArgsConstructor
public class SysMenuController {

    private final ISysMenuService sysMenuService;

    @GetMapping("/tree")
    @Operation(summary = "获取菜单树", description = "获取完整菜单树结构")
    public R<List<SysMenu>> getMenuList() {
        return R.success(sysMenuService.getMenuTree());
    }

    @GetMapping("/my-tree")
    @Operation(summary = "当前用户授权菜单树", description = "按当前登录用户角色授权返回可路由菜单（目录/菜单，不含按钮），供前端动态路由使用；超级管理员返回全部")
    public R<List<SysMenu>> getMyMenuTree() {
        return R.success(sysMenuService.getMyMenuTree());
    }

    @GetMapping("/{id}")
    @Operation(summary = "通过id查询", description = "通过id查询菜单详情")
    public R<SysMenu> getById(@PathVariable("id") Long id) {
        return R.success(sysMenuService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("#clientId:menu:add")
    @Operation(summary = "新增", description = "新增菜单")
    public R<Boolean> save(@RequestBody @Valid SysMenuCreateReq params) {
        String clientId = ClientContextHolder.getClientId();
        SysMenu sysMenu = BeanUtil.copyProperties(params, SysMenu.class);
        sysMenu.setClientId(clientId);
        return R.success(sysMenuService.save(sysMenu));
    }

    @PutMapping
    @SaCheckPermission("#clientId:menu:update")
    @Operation(summary = "更新", description = "更新菜单")
    public R<Boolean> updateById(@RequestBody @Valid SysMenuUpdateReq params) {
        SysMenu sysMenu = BeanUtil.copyProperties(params, SysMenu.class);
        return R.success(sysMenuService.updateById(sysMenu));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("#clientId:menu:delete")
    @Operation(summary = "删除", description = "删除菜单（含子菜单）")
    public R<Boolean> removeById(@PathVariable("id") Long id) {
        List<SysMenu> allMenus = sysMenuService.getMenuTree();
        deleteWithChildren(id, allMenus);
        return R.success(true);
    }

    @GetMapping("/exists/name")
    @Operation(summary = "检查菜单名称是否存在")
    public R<Boolean> isNameExists(@RequestParam("name") @Size(max = 50, message = "{menu.name.max}") String name,
                                    @RequestParam(value = "excludeId", required = false) Long excludeId) {
        return R.success(sysMenuService.isNameExists(name, excludeId));
    }

    @GetMapping("/exists/path")
    @Operation(summary = "检查路由路径是否存在")
    public R<Boolean> isPathExists(@RequestParam("path") @Size(max = 255, message = "{menu.path.size}") String path,
                                    @RequestParam(value = "excludeId", required = false) Long excludeId) {
        return R.success(sysMenuService.isPathExists(path, excludeId));
    }

    private void deleteWithChildren(Long id, List<SysMenu> menus) {
        for (SysMenu menu : menus) {
            if (menu.getId().equals(id)) {
                sysMenuService.removeById(id);
                if (menu.getChildren() != null) {
                    for (SysMenu child : menu.getChildren()) {
                        deleteWithChildren(child.getId(), List.of(child));
                    }
                }
                return;
            }
            if (menu.getChildren() != null) {
                deleteWithChildren(id, menu.getChildren());
            }
        }
    }
}
