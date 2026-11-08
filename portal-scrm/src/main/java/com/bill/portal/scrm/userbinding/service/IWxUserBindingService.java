package com.bill.portal.scrm.userbinding.service;

import com.bill.portal.scrm.userbinding.dto.WxUserBindingSaveReq;
import com.bill.portal.scrm.userbinding.entity.WxUserBinding;
import com.bill.portal.scrm.userbinding.vo.WxUserBindingVo;

import java.util.List;

/**
 * 系统用户-企微成员绑定服务（数据权限前置：
 * 业务数据归属企微成员，经绑定映射到系统账号后按系统部门过滤）。
 * 一个租户可授权多家企业，同一系统账号可分别绑定其在每家企业下的成员
 * （唯一粒度：租户+企业+账号 / 租户+企业+成员，每企业内双向 1:1）
 */
public interface IWxUserBindingService {

    /** 当前租户全部绑定（跨企业，带成员姓名与账号信息） */
    List<WxUserBindingVo> list();

    /**
     * 绑定/换绑：同企业内 账号↔成员 双向 1:1，
     * 同一账号在同一企业重复绑定视为换绑；目标成员已被其他账号绑定则拒绝
     */
    void bind(WxUserBindingSaveReq req);

    /** 解绑 */
    void unbind(Long id);

    /** 按系统用户ID查询其全部绑定（跨企业，数据权限拦截器读取入口） */
    List<WxUserBinding> listBySysUserId(Long sysUserId);
}
