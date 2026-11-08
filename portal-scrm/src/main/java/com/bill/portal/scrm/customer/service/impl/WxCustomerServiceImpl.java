package com.bill.portal.scrm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bill.portal.scrm.customer.dto.WxCustomerQuery;
import com.bill.portal.scrm.customer.dto.WxCustomerStatDTO;
import com.bill.portal.scrm.customer.entity.WxCustomer;
import com.bill.portal.scrm.customer.mapper.WxCustomerMapper;
import com.bill.portal.scrm.customer.service.IWxCustomerService;
import com.bill.portal.scrm.contact.service.IWxUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class WxCustomerServiceImpl extends ServiceImpl<WxCustomerMapper, WxCustomer> implements IWxCustomerService {

    private final IWxUserService wxUserService;

    @Override
    public IPage<WxCustomer> page(Page<WxCustomer> page, WxCustomerQuery query) {
        return baseMapper.selectPage(page, buildWrapper(query));
    }

    @Override
    public List<WxCustomer> list(WxCustomerQuery query) {
        return baseMapper.selectList(buildWrapper(query));
    }

    /** 条件仅对非空字段生效：corpId 精确、userId/名称/流失状态；deptId 命中部门及子部门下的跟进成员 */
    private LambdaQueryWrapper<WxCustomer> buildWrapper(WxCustomerQuery query) {
        LambdaQueryWrapper<WxCustomer> wrapper = Wrappers.<WxCustomer>lambdaQuery()
                .eq(StringUtils.hasText(query.getCorpId()), WxCustomer::getCorpId, query.getCorpId())
                .eq(StringUtils.hasText(query.getUserId()), WxCustomer::getUserId, query.getUserId())
                .like(StringUtils.hasText(query.getName()), WxCustomer::getName, query.getName())
                .eq(query.getLostFlag() != null, WxCustomer::getLostFlag, query.getLostFlag());
        if (query.getDeptId() != null) {
            List<String> deptUserIds = wxUserService
                    .listUserIdsByDeptTree(query.getCorpId(), query.getDeptId());
            if (deptUserIds.isEmpty()) {
                // 部门下无成员：恒假条件返回空集
                return wrapper.apply("1=0");
            }
            wrapper.in(WxCustomer::getUserId, deptUserIds);
        }
        // 客户标签：extra_data 快照里任一标签的 tagId 命中即返回
        if (StringUtils.hasText(query.getTagId())) {
            wrapper.apply(
                    "JSON_OVERLAPS(JSON_EXTRACT(extra_data, '$.followedUsers[*].tags[*].tagId'), JSON_ARRAY({0}))",
                    query.getTagId());
        }
        return wrapper.orderByAsc(WxCustomer::getId);
    }

    /**
     * 客户统计-当日汇总（实时口径：本地表即时计算）
     */
    @Override
    public WxCustomerStatDTO statisticSummary(String corpId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        WxCustomerStatDTO dto = new WxCustomerStatDTO();
        dto.setTotal(count(Wrappers.<WxCustomer>lambdaQuery()
                .eq(WxCustomer::getCorpId, corpId)));
        dto.setTodayNew(count(Wrappers.<WxCustomer>lambdaQuery()
                .eq(WxCustomer::getCorpId, corpId)
                .ge(WxCustomer::getFollowCreateTime, todayStart)));
        dto.setTodayLost(count(Wrappers.<WxCustomer>lambdaQuery()
                .eq(WxCustomer::getCorpId, corpId)
                .eq(WxCustomer::getLostFlag, 1)
                .ge(WxCustomer::getLostTime, todayStart)));
        dto.setLostTotal(count(Wrappers.<WxCustomer>lambdaQuery()
                .eq(WxCustomer::getCorpId, corpId)
                .eq(WxCustomer::getLostFlag, 1)));
        return dto;
    }
}
