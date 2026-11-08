package com.bill.portal.scrm.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bill.portal.scrm.common.entity.WxSyncVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WxSyncVersionMapper extends BaseMapper<WxSyncVersion> {

    /**
     * 查询当前最大版本号并加行锁，防止并发插入重复号
     */
    @Select("SELECT MAX(version_no) FROM wx_sync_version WHERE corp_id = #{corpId} FOR UPDATE")
    Long selectMaxVersionNoForUpdate(@Param("corpId") String corpId);


}
