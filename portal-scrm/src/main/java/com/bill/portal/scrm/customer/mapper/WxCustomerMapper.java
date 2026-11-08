package com.bill.portal.scrm.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bill.portal.scrm.customer.entity.WxCustomer;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WxCustomerMapper extends BaseMapper<WxCustomer> {

    /**
     * 批量新增或更新
     * 基于 ON DUPLICATE KEY UPDATE 实现高性能写入
     * 触发条件基于联合唯一键 (tenant_id, corp_id,external_user_id,user_id)
     */
    void batchUpsert(List<WxCustomer> list);
}
