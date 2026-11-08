package com.bill.portal.system.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bill.portal.system.entity.SysRole;

import java.io.Serializable;

public interface ISysRoleService extends IService<SysRole> {

    /**
     * 检查角色名称是否已存在
     */
    boolean isNameExists(String name, Long excludeId);

    /**
     * 分页查询并回填每行的 permissions / deptIds。
     * IService.page(page, wrapper) 不做任何关联回填，列表接口必须走本方法
     */
    <E extends IPage<SysRole>> E pageWithRelations(E page, Wrapper<SysRole> wrapper);

    @Override
    boolean save(SysRole entity);

    @Override
    boolean updateById(SysRole entity);

    @Override
    boolean removeById(Serializable id);

    @Override
    SysRole getById(Serializable id);
}
