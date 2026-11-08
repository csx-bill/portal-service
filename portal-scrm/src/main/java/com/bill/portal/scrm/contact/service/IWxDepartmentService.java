package com.bill.portal.scrm.contact.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bill.portal.scrm.contact.entity.WxDepartment;
import com.bill.portal.scrm.contact.vo.WxDepartmentTreeVO;

import java.util.List;

public interface IWxDepartmentService extends IService<WxDepartment> {

    /**
     * 获取部门树
     *
     * @return 部门树列表，不同企业的根节点平级
     */
    List<WxDepartmentTreeVO> deptTree();
}
