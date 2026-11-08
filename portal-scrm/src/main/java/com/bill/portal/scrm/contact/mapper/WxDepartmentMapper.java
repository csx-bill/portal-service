package com.bill.portal.scrm.contact.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bill.portal.scrm.contact.entity.WxDepartment;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface WxDepartmentMapper extends BaseMapper<WxDepartment> {

    /**
     * 批量新增或更新
     * 基于 ON DUPLICATE KEY UPDATE 实现高性能写入
     * 触发条件基于联合唯一键 (tenant_id,corp_id, userid)
     */
    void batchUpsert(List<WxDepartment> list);
}
