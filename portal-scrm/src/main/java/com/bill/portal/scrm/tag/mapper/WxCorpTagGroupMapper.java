package com.bill.portal.scrm.tag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bill.portal.scrm.tag.entity.WxCorpTagGroup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WxCorpTagGroupMapper extends BaseMapper<WxCorpTagGroup> {

    /**
     * 批量新增或更新
     * 基于 ON DUPLICATE KEY UPDATE 实现高性能写入
     * 触发条件基于联合唯一键 (tenant_id, corp_id, group_id)
     */
    void batchUpsert(List<WxCorpTagGroup> list);
}
