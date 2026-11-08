package com.bill.portal.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bill.portal.system.entity.SysDept;

import java.util.List;

public interface ISysDeptService extends IService<SysDept> {

    /**
     * 部门树（当前租户全量，按 sortOrder 升序组装）
     */
    List<SysDept> tree();

    /**
     * 指定部门及其全部子部门的 ID 列表（用于按部门过滤用户）
     */
    List<Long> listSelfAndChildIds(Long deptId);
}
