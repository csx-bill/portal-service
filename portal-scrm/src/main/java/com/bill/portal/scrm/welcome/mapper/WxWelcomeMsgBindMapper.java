package com.bill.portal.scrm.welcome.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bill.portal.scrm.welcome.entity.WxWelcomeMsgBind;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/** 企微欢迎语绑定 Mapper */
@Mapper
public interface WxWelcomeMsgBindMapper extends BaseMapper<WxWelcomeMsgBind> {

    /**
     * 绑定 upsert：唯一键 (corp_id, target_type, target_id, welcome_msg_id)
     * 命中时幂等恢复（逻辑删除的旧绑定行复活），避免重复绑定撞唯一键。
     */
    @Insert("INSERT INTO wx_welcome_msg_bind "
            + "(id, corp_id, target_type, target_id, welcome_msg_id, del_flag, create_time, update_time, tenant_id) "
            + "VALUES (#{id}, #{corpId}, #{targetType}, #{targetId}, #{welcomeMsgId}, 0, NOW(), NOW(), #{tenantId}) "
            + "ON DUPLICATE KEY UPDATE welcome_msg_id = VALUES(welcome_msg_id), del_flag = 0, update_time = NOW()")
    int upsertBind(WxWelcomeMsgBind bind);
}
