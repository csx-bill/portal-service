package com.bill.portal.scrm.contact.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bill.portal.scrm.contact.dto.WxUserQuery;
import com.bill.portal.scrm.contact.entity.WxUser;

import java.util.List;

public interface IWxUserService extends IService<WxUser> {

    /**
     * 分页查询成员（过滤条件取自 WxUserQuery 非空字段：
     * name 模糊 / status / corpId 精确 / deptId 命中成员任一所属部门）
     */
    IPage<WxUser> page(Page<WxUser> page, WxUserQuery query);

    /**
     * 列表查询成员（过滤条件同 {@link #page}，不分页）
     */
    List<WxUser> list(WxUserQuery query);

    /**
     * 部门（含子部门）下的成员企微 userid 列表（按成员主部门归属）。
     * 供左侧部门树选中后，无部门查询条件的列表接口统一做成员 IN 过滤
     */
    List<String> listUserIdsByDeptTree(String corpId, Long deptId);
}
