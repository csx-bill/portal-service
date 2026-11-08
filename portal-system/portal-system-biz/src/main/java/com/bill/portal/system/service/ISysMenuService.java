package com.bill.portal.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bill.portal.system.entity.SysMenu;

import java.util.List;

public interface ISysMenuService extends IService<SysMenu> {

    /**
     * 获取菜单树
     */
    List<SysMenu> getMenuTree();

    /**
     * 获取当前登录用户角色授权的菜单树（目录/菜单，不含按钮），
     * 供前端动态路由使用；超级管理员返回全部
     */
    List<SysMenu> getMyMenuTree();

    /**
     * 检查菜单名称是否已存在
     */
    boolean isNameExists(String name, Long excludeId);

    /**
     * 检查路由路径是否已存在
     */
    boolean isPathExists(String path, Long excludeId);

}
