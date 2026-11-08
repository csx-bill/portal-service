package com.bill.portal.scrm.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface WxDashboardMapper {

    /** 客户总数 */
    int countCustomer(@Param("corpId") String corpId);

    /** 窗口内流失客户数（lost_flag=1 且流失时间在窗口内） */
    int countCustomerLostSince(@Param("corpId") String corpId, @Param("since") LocalDateTime since);

    /** 按日新增客户 */
    List<Map<String, Object>> countNewCustomerByDay(@Param("corpId") String corpId, @Param("since") LocalDateTime since);

    /** 按日新增群（COALESCE(group_create_time, create_time)，存续群） */
    List<Map<String, Object>> countNewGroupByDay(@Param("corpId") String corpId, @Param("since") LocalDateTime since);

    /** 按日新增群人数（join_time） */
    List<Map<String, Object>> countNewMemberByDay(@Param("corpId") String corpId, @Param("since") LocalDateTime since);

    /** 按日退群人数（成员变动流水 change_type=2） */
    List<Map<String, Object>> countQuitMemberByDay(@Param("corpId") String corpId, @Param("since") LocalDateTime since);

    /** 存续群总数与群成员总数 */
    Map<String, Object> groupTotals(@Param("corpId") String corpId);

    /** 窗口内群发送达聚合（total/delivered），按调用记录时间窗过滤 */
    Map<String, Object> massRateSince(@Param("corpId") String corpId, @Param("since") LocalDateTime since, @Param("until") LocalDateTime until);

    /** 窗口内朋友圈发表聚合（total/published），按调用记录时间窗过滤 */
    Map<String, Object> momentRateSince(@Param("corpId") String corpId, @Param("since") LocalDateTime since, @Param("until") LocalDateTime until);

    /** 成员跟进客户数排行 */
    List<Map<String, Object>> memberRank(@Param("corpId") String corpId, @Param("limit") int limit);
}
