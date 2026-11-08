package com.bill.portal.scrm.groupchat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bill.portal.scrm.groupchat.entity.WxGroupChatMemberLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 群成员变动记录 Mapper
 */
@Mapper
public interface WxGroupChatMemberLogMapper extends BaseMapper<WxGroupChatMemberLog> {

}
