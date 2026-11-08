package com.bill.portal.scrm.groupchat.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bill.portal.scrm.groupchat.dto.GroupChatStatisticDTO;
import com.bill.portal.scrm.groupchat.dto.GroupMemberStatisticDTO;
import com.bill.portal.scrm.groupchat.dto.WxGroupChatQuery;
import com.bill.portal.scrm.groupchat.entity.WxGroupChat;
import com.bill.portal.scrm.groupchat.entity.WxGroupChatMember;

import java.time.LocalDate;
import java.util.List;

public interface IWxGroupChatService extends IService<WxGroupChat> {

    /**
     * 分页查询客户群（过滤条件取自 WxGroupChatQuery 非空字段：corpId 精确 / chatName 模糊）
     */
    IPage<WxGroupChat> page(Page<WxGroupChat> page, WxGroupChatQuery query);

    /**
     * 列表查询客户群（过滤条件同 {@link #page}，不分页）
     */
    List<WxGroupChat> list(WxGroupChatQuery query);

    /**
     * 查询群成员列表（按入群时间升序）
     * @param chatId 客户群ID（企微 chat_id）
     */
    List<WxGroupChatMember> listMembers(String chatId);

    /**
     * 群成员变动统计（指定日期的入群/退群数量，date 为空默认当天）
     * @param corpId 企业ID
     * @param chatId 客户群ID（企微 chat_id）
     * @param date 统计日期
     */
    GroupMemberStatisticDTO memberStatistic(String corpId, String chatId, LocalDate date);

    /**
     * 按企业聚合查询各群成员变动统计（指定日期，date 为空默认当天）
     * @param corpId 企业ID
     * @param date 统计日期
     * @return 每个客户群一条统计（chatId + 入群/退群数量）
     */
    List<GroupMemberStatisticDTO> memberStatisticByCorp(String corpId, LocalDate date);

    /**
     * 客户群数据统计——当日汇总（合计全部群主）
     * @param corpId 企业ID
     * @param date 统计日期（为空默认当天）
     */
    GroupChatStatisticDTO statisticSummary(String corpId, LocalDate date);
}
