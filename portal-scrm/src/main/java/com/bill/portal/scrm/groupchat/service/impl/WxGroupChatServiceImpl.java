package com.bill.portal.scrm.groupchat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bill.portal.scrm.groupchat.dto.GroupChatStatisticDTO;
import com.bill.portal.scrm.groupchat.dto.GroupMemberStatisticDTO;
import com.bill.portal.scrm.groupchat.dto.WxGroupChatQuery;
import com.bill.portal.scrm.groupchat.entity.WxGroupChat;
import com.bill.portal.scrm.groupchat.entity.WxGroupChatMember;
import com.bill.portal.scrm.groupchat.entity.WxGroupChatMemberLog;
import com.bill.portal.scrm.groupchat.mapper.WxGroupChatMapper;
import com.bill.portal.scrm.groupchat.mapper.WxGroupChatMemberMapper;
import com.bill.portal.scrm.groupchat.mapper.WxGroupChatMemberLogMapper;
import com.bill.portal.scrm.groupchat.service.IWxGroupChatService;
import com.bill.portal.scrm.contact.service.IWxUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class WxGroupChatServiceImpl extends ServiceImpl<WxGroupChatMapper, WxGroupChat> implements IWxGroupChatService {

    private final WxGroupChatMemberMapper wxGroupChatMemberMapper;

    private final WxGroupChatMemberLogMapper wxGroupChatMemberLogMapper;

    private final IWxUserService wxUserService;

    @Override
    public IPage<WxGroupChat> page(Page<WxGroupChat> page, WxGroupChatQuery query) {
        return baseMapper.selectPage(page, buildWrapper(query));
    }

    @Override
    public List<WxGroupChat> list(WxGroupChatQuery query) {
        return baseMapper.selectList(buildWrapper(query));
    }

    @Override
    public List<WxGroupChatMember> listMembers(String chatId) {
        return wxGroupChatMemberMapper.selectList(Wrappers.<WxGroupChatMember>lambdaQuery()
                .eq(WxGroupChatMember::getChatId, chatId)
                .orderByAsc(WxGroupChatMember::getJoinTime));
    }

    @Override
    public GroupMemberStatisticDTO memberStatistic(String corpId, String chatId, LocalDate date) {
        LocalDate day = date != null ? date : LocalDate.now();
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        GroupMemberStatisticDTO dto = new GroupMemberStatisticDTO();
        // 入群：按成员表 join_time 统计（企微权威入群时间，实时）
        dto.setAddCount(wxGroupChatMemberMapper.selectCount(
                Wrappers.<WxGroupChatMember>lambdaQuery()
                        .eq(WxGroupChatMember::getChatId, chatId)
                        .ge(WxGroupChatMember::getJoinTime, start)
                        .lt(WxGroupChatMember::getJoinTime, end)));
        // 退群：变动流水（企微无退群时间戳，以本地发现时间近似）
        dto.setDelCount(wxGroupChatMemberLogMapper.selectCount(
                Wrappers.<WxGroupChatMemberLog>lambdaQuery()
                        .eq(WxGroupChatMemberLog::getChatId, chatId)
                        .eq(WxGroupChatMemberLog::getChangeType, 2)
                        .ge(WxGroupChatMemberLog::getChangeTime, start)
                        .lt(WxGroupChatMemberLog::getChangeTime, end)));
        return dto;
    }

    @Override
    public List<GroupMemberStatisticDTO> memberStatisticByCorp(String corpId, LocalDate date) {
        LocalDate day = date != null ? date : LocalDate.now();
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        Map<String, GroupMemberStatisticDTO> byChat = new LinkedHashMap<>();

        // 入群：成员表 join_time 按群聚合（企微权威入群时间，实时）
        List<WxGroupChatMember> joined = wxGroupChatMemberMapper.selectList(
                Wrappers.<WxGroupChatMember>lambdaQuery()
                        .eq(WxGroupChatMember::getCorpId, corpId)
                        .ge(WxGroupChatMember::getJoinTime, start)
                        .lt(WxGroupChatMember::getJoinTime, end));
        for (WxGroupChatMember member : joined) {
            GroupMemberStatisticDTO dto = byChat.computeIfAbsent(
                    member.getChatId(),
                    key -> {
                        GroupMemberStatisticDTO item = new GroupMemberStatisticDTO();
                        item.setChatId(key);
                        item.setAddCount(0L);
                        item.setDelCount(0L);
                        return item;
                    });
            dto.setAddCount(dto.getAddCount() + 1);
        }

        // 退群：变动流水按群聚合
        List<WxGroupChatMemberLog> logs = wxGroupChatMemberLogMapper.selectList(
                Wrappers.<WxGroupChatMemberLog>lambdaQuery()
                        .eq(WxGroupChatMemberLog::getCorpId, corpId)
                        .eq(WxGroupChatMemberLog::getChangeType, 2)
                        .ge(WxGroupChatMemberLog::getChangeTime, start)
                        .lt(WxGroupChatMemberLog::getChangeTime, end));
        for (WxGroupChatMemberLog log : logs) {
            GroupMemberStatisticDTO dto = byChat.computeIfAbsent(
                    log.getChatId(),
                    key -> {
                        GroupMemberStatisticDTO item = new GroupMemberStatisticDTO();
                        item.setChatId(key);
                        item.setAddCount(0L);
                        item.setDelCount(0L);
                        return item;
                    });
            dto.setDelCount(dto.getDelCount() + 1);
        }
        return new ArrayList<>(byChat.values());
    }

    /**
     * 统计卡片汇总——实时口径（本地表即时计算，非 T+1 档案）：
     * - 新增群/新增人数：当天本地新发现/变动流水
     * - 群总数/群总人数：当前存续群（不含已解散）
     */
    @Override
    public GroupChatStatisticDTO statisticSummary(String corpId, LocalDate date) {
        LocalDate day = date != null ? date : LocalDate.now();
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        GroupChatStatisticDTO dto = new GroupChatStatisticDTO();

        // 存续群（排除已解散）：群总数与群总人数实时合计
        List<WxGroupChat> groups = baseMapper.selectList(
                Wrappers.<WxGroupChat>lambdaQuery()
                        .eq(WxGroupChat::getCorpId, corpId)
                        .eq(WxGroupChat::getDelFlag, 0)
                        .and(w -> w.eq(WxGroupChat::getDismissFlag, 0)
                                .or()
                                .isNull(WxGroupChat::getDismissFlag)));
        dto.setChatTotal(groups.size());
        dto.setMemberTotal(groups.stream()
                .mapToInt(g -> g.getMemberNum() == null ? 0 : g.getMemberNum())
                .sum());

        // 当日新增群：按企微群创建时间统计（未回拉到的回退本地发现时间）
        dto.setNewChatCnt((int) groups.stream()
                .filter(g -> effectiveCreated(g).isAfter(start))
                .count());

        // 当日新增人数：成员表 join_time 统计（企微权威入群时间，实时）
        dto.setNewMemberCnt(Math.toIntExact(wxGroupChatMemberMapper.selectCount(
                Wrappers.<WxGroupChatMember>lambdaQuery()
                        .eq(WxGroupChatMember::getCorpId, corpId)
                        .ge(WxGroupChatMember::getJoinTime, start)
                        .lt(WxGroupChatMember::getJoinTime, end))));

        // 当日退群人数：成员变动流水（退群无企微时间戳，以本地发现时间近似）
        dto.setDelMemberCnt(Math.toIntExact(wxGroupChatMemberLogMapper.selectCount(
                Wrappers.<WxGroupChatMemberLog>lambdaQuery()
                        .eq(WxGroupChatMemberLog::getCorpId, corpId)
                        .eq(WxGroupChatMemberLog::getChangeType, 2)
                        .ge(WxGroupChatMemberLog::getChangeTime, start))));
        return dto;
    }

    /** 群的有效创建时间：优先企微返回的群创建时间，回退本地发现时间 */
    private LocalDateTime effectiveCreated(WxGroupChat group) {
        LocalDateTime created = group.getGroupCreateTime() != null
                ? group.getGroupCreateTime()
                : group.getCreateTime();
        return created == null ? LocalDateTime.MIN : created;
    }

    /** 条件仅对非空字段生效：corpId/owner 精确、chatName 模糊；deptId 命中部门及子部门下的群主 */
    private LambdaQueryWrapper<WxGroupChat> buildWrapper(WxGroupChatQuery query) {
        LambdaQueryWrapper<WxGroupChat> wrapper = Wrappers.<WxGroupChat>lambdaQuery()
                .eq(StringUtils.hasText(query.getCorpId()), WxGroupChat::getCorpId, query.getCorpId())
                .eq(StringUtils.hasText(query.getOwner()), WxGroupChat::getOwner, query.getOwner())
                .like(StringUtils.hasText(query.getChatName()), WxGroupChat::getChatName, query.getChatName());
        if (query.getDeptId() != null) {
            List<String> deptUserIds = wxUserService
                    .listUserIdsByDeptTree(query.getCorpId(), query.getDeptId());
            if (deptUserIds.isEmpty()) {
                // 部门下无成员：恒假条件返回空集
                return wrapper.apply("1=0");
            }
            wrapper.in(WxGroupChat::getOwner, deptUserIds);
        }
        return wrapper.orderByAsc(WxGroupChat::getId);
    }
}
