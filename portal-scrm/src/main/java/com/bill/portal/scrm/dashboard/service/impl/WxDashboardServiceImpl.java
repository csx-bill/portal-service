package com.bill.portal.scrm.dashboard.service.impl;

import com.bill.portal.scrm.contact.entity.WxUser;
import com.bill.portal.scrm.contact.mapper.WxUserMapper;
import com.bill.portal.scrm.dashboard.dto.WxDashboardMemberRankVO;
import com.bill.portal.scrm.dashboard.dto.WxDashboardSummaryVO;
import com.bill.portal.scrm.dashboard.dto.WxDashboardTrendVO;
import com.bill.portal.scrm.dashboard.mapper.WxDashboardMapper;
import com.bill.portal.scrm.dashboard.service.IWxDashboardService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作台看板聚合服务（只读查询，全部为本地表实时口径）
 */
@Slf4j
@Service
@AllArgsConstructor
public class WxDashboardServiceImpl implements IWxDashboardService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final WxDashboardMapper wxDashboardMapper;

    private final WxUserMapper wxUserMapper;

    @Override
    public WxDashboardSummaryVO summary(String corpId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        LocalDateTime prevSince = LocalDateTime.now().minusDays(days * 2L);

        WxDashboardSummaryVO vo = new WxDashboardSummaryVO();

        // 客户：总数 + 窗口内新增/流失（上期总数 = 当前 - 新增 + 流失，近似窗口起点）
        vo.setCustomerTotal((long) wxDashboardMapper.countCustomer(corpId));
        long newCustomer = wxDashboardMapper.countNewCustomerByDay(corpId, since)
                .stream().mapToLong(row -> cnt(row)).sum();
        long lost = wxDashboardMapper.countCustomerLostSince(corpId, since);
        vo.setNewCustomer(newCustomer);
        vo.setNewCustomerPrev(countDaySum(wxDashboardMapper.countNewCustomerByDay(corpId, prevSince), since));
        vo.setCustomerTotalPrev(Math.max(0, vo.getCustomerTotal() - newCustomer + lost));

        // 群：存续群总数/群成员总数，上期 = 当前 - 窗口内净增
        Map<String, Object> groupTotals = wxDashboardMapper.groupTotals(corpId);
        long chatTotal = nl(groupTotals != null ? groupTotals.get("chatTotal") : null);
        long memberTotal = nl(groupTotals != null ? groupTotals.get("memberTotal") : null);
        long newGroup = wxDashboardMapper.countNewGroupByDay(corpId, since)
                .stream().mapToLong(row -> cnt(row)).sum();
        long newMember = wxDashboardMapper.countNewMemberByDay(corpId, since)
                .stream().mapToLong(row -> cnt(row)).sum();
        long quit = wxDashboardMapper.countQuitMemberByDay(corpId, since)
                .stream().mapToLong(row -> cnt(row)).sum();
        vo.setGroupTotal(chatTotal);
        vo.setGroupTotalPrev(Math.max(0, chatTotal - newGroup));
        vo.setMemberTotal(memberTotal);
        vo.setMemberTotalPrev(Math.max(0, memberTotal - (newMember - quit)));

        // 运营效果：窗口内 / 上期同长度窗口
        Map<String, Object> massCur = wxDashboardMapper.massRateSince(corpId, since, null);
        Map<String, Object> massPrev = wxDashboardMapper.massRateSince(corpId, prevSince, since);
        vo.setMassDeliveredRate(deliveredRate(massCur));
        vo.setMassDeliveredRatePrev(deliveredRate(massPrev));

        Map<String, Object> momentCur = wxDashboardMapper.momentRateSince(corpId, since, null);
        Map<String, Object> momentPrev = wxDashboardMapper.momentRateSince(corpId, prevSince, since);
        vo.setMomentPublishRate(publishRate(momentCur));
        vo.setMomentPublishRatePrev(publishRate(momentPrev));
        return vo;
    }

    @Override
    public List<WxDashboardTrendVO> trend(String corpId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Map<String, Long> custByDay = toDayMap(wxDashboardMapper.countNewCustomerByDay(corpId, since));
        Map<String, Long> groupByDay = toDayMap(wxDashboardMapper.countNewGroupByDay(corpId, since));
        Map<String, Long> memberByDay = toDayMap(wxDashboardMapper.countNewMemberByDay(corpId, since));
        Map<String, Long> quitByDay = toDayMap(wxDashboardMapper.countQuitMemberByDay(corpId, since));

        List<WxDashboardTrendVO> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            String date = today.minusDays(i).format(DATE_FMT);
            WxDashboardTrendVO row = new WxDashboardTrendVO();
            row.setDate(date);
            row.setNewCustomer(custByDay.getOrDefault(date, 0L));
            row.setNewGroup(groupByDay.getOrDefault(date, 0L));
            row.setNewMember(memberByDay.getOrDefault(date, 0L));
            row.setQuitMember(quitByDay.getOrDefault(date, 0L));
            list.add(row);
        }
        return list;
    }

    @Override
    public List<WxDashboardMemberRankVO> memberRank(String corpId, int limit) {
        List<Map<String, Object>> rows = wxDashboardMapper.memberRank(corpId, limit);
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        // 批量解析成员姓名（企微 userid → wx_user.name）
        List<String> userIds = rows.stream()
                .map(row -> String.valueOf(row.get("userId")))
                .collect(Collectors.toList());
        Map<String, String> nameMap = wxUserMapper.selectList(
                        Wrappers.<WxUser>lambdaQuery()
                                .eq(WxUser::getCorpId, corpId)
                                .in(WxUser::getUserId, userIds))
                .stream()
                .filter(user -> user.getUserId() != null)
                .collect(Collectors.toMap(WxUser::getUserId,
                        user -> StringUtils.hasText(user.getName()) ? user.getName() : user.getUserId(),
                        (a, b) -> a));
        return rows.stream().map(row -> {
            WxDashboardMemberRankVO vo = new WxDashboardMemberRankVO();
            String userId = String.valueOf(row.get("userId"));
            vo.setUserId(userId);
            vo.setName(nameMap.getOrDefault(userId, userId));
            vo.setCustomerCnt(nl(row.get("cnt")));
            return vo;
        }).collect(Collectors.toList());
    }

    private long countDaySum(List<Map<String, Object>> rows, LocalDateTime until) {
        String untilDate = until.toLocalDate().format(DATE_FMT);
        return rows.stream()
                .filter(row -> {
                    Object date = row.get("statDate");
                    return date != null && !String.valueOf(date).equals(untilDate);
                })
                .mapToLong(row -> cnt(row))
                .sum();
    }

    private Map<String, Long> toDayMap(List<Map<String, Object>> rows) {
        Map<String, Long> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object date = row.get("statDate");
            if (date != null) {
                map.put(String.valueOf(date), cnt(row));
            }
        }
        return map;
    }

    private long cnt(Map<String, Object> row) {
        return nl(row.get("cnt"));
    }

    private long nl(Object value) {
        return value == null ? 0L : Long.parseLong(String.valueOf(value));
    }

    private Double deliveredRate(Map<String, Object> row) {
        return pct(row != null ? nl(row.get("deliveredCnt")) : 0,
                row != null ? nl(row.get("totalCnt")) : 0);
    }

    private Double publishRate(Map<String, Object> row) {
        return pct(row != null ? nl(row.get("publishedCnt")) : 0,
                row != null ? nl(row.get("totalCnt")) : 0);
    }

    private Double pct(long part, long total) {
        return total == 0 ? null : Math.round(part * 1000.0 / total) / 10.0;
    }
}
