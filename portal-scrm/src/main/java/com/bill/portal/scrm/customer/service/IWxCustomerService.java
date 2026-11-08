package com.bill.portal.scrm.customer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bill.portal.scrm.customer.dto.WxCustomerQuery;
import com.bill.portal.scrm.customer.dto.WxCustomerStatDTO;
import com.bill.portal.scrm.customer.entity.WxCustomer;

import java.util.List;

public interface IWxCustomerService extends IService<WxCustomer> {

    /**
     * 分页查询客户（过滤条件取自 WxCustomerQuery 非空字段：corpId 精确 / name 模糊）
     */
    IPage<WxCustomer> page(Page<WxCustomer> page, WxCustomerQuery query);

    /**
     * 列表查询客户（过滤条件同 {@link #page}，不分页）
     */
    List<WxCustomer> list(WxCustomerQuery query);

    /**
     * 客户统计-当日汇总（实时口径：本地表即时计算）
     */
    WxCustomerStatDTO statisticSummary(String corpId);
}
