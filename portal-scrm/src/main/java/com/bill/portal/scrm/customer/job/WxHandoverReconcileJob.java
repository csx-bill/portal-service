package com.bill.portal.scrm.customer.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bill.portal.common.core.datascope.DataScopeBroker;
import com.bill.portal.common.core.tenant.TenantBroker;
import com.bill.portal.scrm.customer.entity.WxCustomer;
import com.bill.portal.scrm.customer.mapper.WxCustomerMapper;
import com.bill.portal.scrm.inheritance.dto.TransferResultItemDTO;
import com.bill.portal.scrm.inheritance.dto.TransferResultQueryDTO;
import com.bill.portal.scrm.inheritance.dto.TransferResultRespDTO;
import com.bill.portal.scrm.inheritance.service.IWxInheritanceService;
import com.bill.portal.scrm.customer.service.IWxCustomerSyncService;
import com.bill.portal.scrm.corp.entity.WxCorpAuth;
import com.bill.portal.scrm.corp.service.IWxCorpAuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 客户交接对账任务（兜底，每小时执行）。
 *
 * <p>发起继承分配时会在原成员关系行写"客户交接给成员 X（xx继承已发起）"
 * 注释；正常情况下 del_follow_user / add_external_contact 回调会把行推进
 * 到终态。若回调丢失、顺序异常，或离职继承成功时企微不触发 del 事件，
 * 行会停留在"未流失 + 注释"状态。
 *
 * <p>本任务对发起超过 25 小时（企微接替等待期为 24 小时）仍未回收的行，
 * 按注释中的继承类型调用企微接替状态查询接口，按查询结果回收终态：
 * 接替完毕→标记交接流失；拒绝→清除注释保持原状；上限→标记接替失败流失；
 * 仍在等待→延后下次对账。事件仅提供实时性，本任务保证终态最终一致。
 */
@Slf4j
@Component
@AllArgsConstructor
public class WxHandoverReconcileJob {

    /** 接替等待期（企微为发起后 24 小时），超过后开始对账 */
    private static final int HANDOVER_WAIT_HOURS = 25;

    private static final Pattern ANNOTATION_PATTERN =
            Pattern.compile("客户交接给成员 (.+?)（(在职|离职)继承已发起）");

    private final WxCustomerMapper wxCustomerMapper;
    private final IWxInheritanceService wxInheritanceService;
    private final IWxCorpAuthService wxCorpAuthService;

    @Scheduled(cron = "0 40 * * * ?")
    public void execute() {
        // 1. 获取需要对账的企业列表
        List<WxCorpAuth> authList = wxCorpAuthService.getAuthorizedCorps();
        if (CollectionUtils.isEmpty(authList)) {
            return;
        }
        // 2. 按企业（租户）遍历对账；无登录上下文，数据权限全量放行
        for (WxCorpAuth auth : authList) {
            try {
                TenantBroker.runAs(auth.getTenantId(), (id) ->
                    DataScopeBroker.skip(() -> executeInternal(auth)));
            } catch (Exception e) {
                log.warn("[handover-reconcile] 企业[{}]对账异常", auth.getCorpId(), e);
            }
        }
    }

    private void executeInternal(WxCorpAuth auth) {
        List<WxCustomer> pending = wxCustomerMapper.selectList(
                Wrappers.<WxCustomer>lambdaQuery()
                        .eq(WxCustomer::getCorpId, auth.getCorpId())
                        .eq(WxCustomer::getLostFlag, 0)
                        .like(WxCustomer::getLostReason, "已发起")
                        .le(WxCustomer::getUpdateTime, LocalDateTime.now().minusHours(HANDOVER_WAIT_HOURS)));
        for (WxCustomer row : pending) {
            try {
                reconcile(row);
            } catch (Exception e) {
                log.warn("[handover-reconcile] 对账失败：externalUserId={}, 行id={}",
                        row.getExternalUserId(), row.getId(), e);
            }
        }
        if (!pending.isEmpty()) {
            log.info("[handover-reconcile] 企业[{}]本轮对账完成，处理行数：{}", auth.getCorpId(), pending.size());
        }
    }

    private void reconcile(WxCustomer row) {
        Matcher matcher = ANNOTATION_PATTERN.matcher(row.getLostReason());
        if (!matcher.find()) {
            // 注释格式不符（历史数据），清除后不再处理
            clearAnnotation(row);
            return;
        }
        String takeoverUserid = matcher.group(1);
        boolean onjob = "在职".equals(matcher.group(2));

        TransferResultQueryDTO query = new TransferResultQueryDTO();
        query.setCorpId(row.getCorpId());
        query.setHandoverUserid(row.getUserId());
        query.setTakeoverUserid(takeoverUserid);
        TransferResultRespDTO resp = onjob
                ? wxInheritanceService.onjobTransferResult(query)
                : wxInheritanceService.resignedTransferResult(query);

        Integer status = resp.getCustomer().stream()
                .filter(item -> row.getExternalUserId().equals(item.getExternalUserid()))
                .map(TransferResultItemDTO::getStatus)
                .findFirst()
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();
        if (status == null) {
            // 查询无接替记录，下轮再试
            defer(row, now);
            return;
        }
        switch (status) {
            case 1 -> markHandoverLost(row, takeoverUserid, onjob, now);   // 接替完毕
            case 3 -> clearAnnotation(row);                                // 客户拒绝，原关系保持
            case 4 -> markTransferFailLost(row, now);                      // 接替成员客户数达上限
            default -> defer(row, now);                                    // 等待接替，延后再次对账
        }
    }

    /** 接替完毕：原成员关系行标记为交接流失（离职按员工侧、在职按转接解除） */
    private void markHandoverLost(WxCustomer row, String takeoverUserid, boolean onjob, LocalDateTime now) {
        int lostBy = onjob ? IWxCustomerSyncService.LOST_BY_INHERIT_TRANSFER : IWxCustomerSyncService.LOST_BY_EMPLOYEE;
        wxCustomerMapper.update(null, Wrappers.<WxCustomer>lambdaUpdate()
                .eq(WxCustomer::getId, row.getId())
                .set(WxCustomer::getLostFlag, 1)
                .set(WxCustomer::getLostBy, lostBy)
                .set(WxCustomer::getLostReason, "客户交接给成员 " + takeoverUserid + "（继承完成）")
                .set(WxCustomer::getLostTime, now)
                .set(WxCustomer::getUpdateTime, now));
        log.info("[handover-reconcile] 继承完成回收终态：externalUserId={}, 行id={}, lostBy={}",
                row.getExternalUserId(), row.getId(), lostBy);
    }

    private void markTransferFailLost(WxCustomer row, LocalDateTime now) {
        wxCustomerMapper.update(null, Wrappers.<WxCustomer>lambdaUpdate()
                .eq(WxCustomer::getId, row.getId())
                .set(WxCustomer::getLostFlag, 1)
                .set(WxCustomer::getLostBy, IWxCustomerSyncService.LOST_BY_TRANSFER_FAIL)
                .set(WxCustomer::getLostReason, "接替成员客户数达上限")
                .set(WxCustomer::getLostTime, now)
                .set(WxCustomer::getUpdateTime, now));
        log.info("[handover-reconcile] 接替失败回收终态：externalUserId={}, 行id={}",
                row.getExternalUserId(), row.getId());
    }

    private void clearAnnotation(WxCustomer row) {
        wxCustomerMapper.update(null, Wrappers.<WxCustomer>lambdaUpdate()
                .eq(WxCustomer::getId, row.getId())
                .set(WxCustomer::getLostReason, null)
                .set(WxCustomer::getUpdateTime, LocalDateTime.now()));
    }

    private void defer(WxCustomer row, LocalDateTime now) {
        wxCustomerMapper.update(null, Wrappers.<WxCustomer>lambdaUpdate()
                .eq(WxCustomer::getId, row.getId())
                .set(WxCustomer::getUpdateTime, now));
    }
}
