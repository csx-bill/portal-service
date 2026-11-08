package com.bill.portal.scrm.acquisition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bill.portal.scrm.acquisition.entity.WxAcquisitionLink;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WxAcquisitionLinkMapper extends BaseMapper<WxAcquisitionLink> {

    /**
     * 批量新增或更新
     * 基于 ON DUPLICATE KEY UPDATE 实现高性能写入
     * 触发条件基于联合唯一键 (link_id, corp_id)
     */
    void batchUpsert(List<WxAcquisitionLink> list);
}
