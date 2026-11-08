package com.bill.portal.scrm.customer.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bill.portal.common.core.exception.CheckedException;
import com.bill.portal.common.core.tenant.TenantContextHolder;
import com.bill.portal.scrm.common.enums.SyncType;
import com.bill.portal.scrm.common.enums.TriggerType;
import com.bill.portal.scrm.common.service.ISyncVersionService;
import com.bill.portal.scrm.contact.entity.WxUser;
import com.bill.portal.scrm.contact.mapper.WxUserMapper;
import com.bill.portal.scrm.corp.strategy.WxCorpApiStrategy;
import com.bill.portal.scrm.customer.dto.WxCustomerMarkTagReq;
import com.bill.portal.scrm.customer.dto.WxCustomerRemarkReq;
import com.bill.portal.scrm.customer.entity.WxCustomer;
import com.bill.portal.scrm.customer.mapper.WxCustomerMapper;
import com.bill.portal.scrm.customer.service.IWxCustomerSyncService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.api.WxCpExternalContactService;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.external.contact.FollowedUser;
import me.chanjar.weixin.cp.bean.external.contact.WxCpExternalContactBatchInfo;
import me.chanjar.weixin.cp.bean.external.contact.WxCpExternalContactInfo;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 企微客户同步核心服务
 */
@Slf4j
@Service
@AllArgsConstructor
public class WxCustomerSyncServiceImpl implements IWxCustomerSyncService {

    private final WxCorpApiStrategy wxCorpApiStrategy;

    private final WxUserMapper wxUserMapper;

    private final WxCustomerMapper wxCustomerMapper;

    private final ISyncVersionService versionService;

    /**
     * 执行全量同步
     * @param tenantId 租户ID
     * @param corpId 企业ID
     * @param syncType 同步类型
     * @param triggerType 触发类型
     */
    @Override
    public void executeFullSync(Long tenantId,String corpId,String syncType, String triggerType) {
        Long currentVersion = versionService.generateNewVersion(corpId, syncType, triggerType);
        try {
            WxCpService wxCpService = wxCorpApiStrategy.getWxCpService(corpId);
            WxCpExternalContactService externalContactService = wxCpService.getExternalContactService();

            List<WxUser> wxUsers = wxUserMapper.selectList(Wrappers.<WxUser>lambdaQuery().eq(WxUser::getCorpId, corpId));

            // 1. 提取所有员工的userId
            List<String> userIdList = wxUsers.stream()
                    .map(WxUser::getUserId)
                    .collect(Collectors.toList());

            // 2. 企微批量接口单次最多支持100个userid
            int batchSize = 100;
            for (int i = 0; i < userIdList.size(); i += batchSize) {
                List<String> batch = userIdList.subList(i, Math.min(i + batchSize, userIdList.size()));
                String[] batchArray = batch.toArray(new String[0]);

                // 3. 针对每一批用户，处理分页游标拉取数据
                String cursor = null;
                do {
                    // 根据文档limit最大可设100
                    WxCpExternalContactBatchInfo batchInfo = externalContactService.getContactDetailBatch(batchArray, cursor, 100);
                    List<WxCpExternalContactBatchInfo.ExternalContactInfo> externalContactList = batchInfo.getExternalContactList();

                    // TODO: 在此处理 batchInfo 中的客户信息，例如转换并保存到数据库
                    syncCustomers(tenantId, corpId, externalContactList, currentVersion);

                    // 获取下一页的游标，如果为空则代表该批次用户数据拉取完毕
                    cursor = batchInfo.getNextCursor();
                    log.info("成功拉取一批客户信息，当前批次起始索引: {}, 本次拉取数: {}", i, batchInfo.getExternalContactList().size());
                } while (StringUtils.hasText(cursor));
            }

            versionService.updateVersionStatus(corpId, syncType, currentVersion, "SUCCESS", null);
            log.info("企业[{}]全量同步完成, version: {}", corpId, currentVersion);
        } catch (Exception e) {
            log.error("企业[{}]全量同步失败, version: {}", corpId, currentVersion, e);
            versionService.updateVersionStatus(corpId, syncType, currentVersion, "FAIL", e.getMessage());
        }
    }

    /**
     * 同步客户信息
     * @param tenantId 租户ID
     * @param corpId
     * @param syncType 同步类型
     * @param triggerType 触发类型
     * @param externalUserId
     * @param userId
     */
    @Override
    public void syncCustomer(Long tenantId,String corpId, String syncType, String triggerType, String externalUserId, String userId) {
        WxCpService wxCpService = wxCorpApiStrategy.getWxCpService(corpId);

        // 2. 生成增量版本号 (与后续DB操作同事务，保证一致性)
        Long currentVersion = versionService.generateNewVersion(corpId, syncType, triggerType);
        try {
            WxCpExternalContactInfo dto = wxCpService.getExternalContactService().getContactDetail(externalUserId, null);
            // 从 externalContact 拷贝客户档案字段（直接拷 Info 对象属性名对不上，档案字段会全为空）
            WxCustomer cust = BeanUtil.copyProperties(dto.getExternalContact(), WxCustomer.class);
            cust.setId(null);
            cust.setCorpId(corpId);
            cust.setSyncVersion(currentVersion);
            // 将企微返回的完整 DTO 转为 Map 存入快照字段
            cust.setExtraData(BeanUtil.beanToMap(dto));
            cust.setTenantId(tenantId);
            cust.setUserId(userId);
            cust.setExternalUserId(externalUserId);
            cust.setCreateTime(LocalDateTime.now());
            cust.setUpdateTime(LocalDateTime.now());
            cust.setDelFlag(0);
            // 该成员添加此外部联系人的时间与渠道标识（follow_user 取当前跟进成员，取不到用首条）
            if (CollUtil.isNotEmpty(dto.getFollowedUsers())) {
                FollowedUser followUser = dto.getFollowedUsers().stream()
                        .filter(f -> userId.equals(f.getUserId()))
                        .findFirst()
                        .or(() -> dto.getFollowedUsers().stream().findFirst())
                        .orElse(null);
                if (followUser != null) {
                    if (followUser.getCreateTime() != null) {
                        cust.setFollowCreateTime(LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(followUser.getCreateTime()), ZoneId.systemDefault()));
                    }
                    cust.setFollowState(followUser.getState());
                }
            }
            // 重新添加：清除流失标记
            cust.setLostFlag(0);
            cust.setLostBy(null);
            cust.setLostReason(null);
            cust.setLostTime(null);
            // 批量 Upsert
            wxCustomerMapper.batchUpsert(Arrays.asList(cust));
            backfillHandoverReason(corpId, externalUserId, userId);
            versionService.updateVersionStatus(corpId, syncType, currentVersion, "SUCCESS", null);
        }catch (Exception e){
            versionService.updateVersionStatus(corpId, syncType, currentVersion, "FAIL", e.getMessage());
        }

    }

    /**
     * 编辑客户企业标签（企微 externalcontact/mark_tag）。
     * 企微打标成功后刷新本地客户快照（extra_data 里的标签随之更新）。
     */
    @Override
    public void markTag(WxCustomerMarkTagReq req) {
        List<String> addTag = req.getAddTag() == null ? List.of() : req.getAddTag();
        List<String> removeTag = req.getRemoveTag() == null ? List.of() : req.getRemoveTag();
        if (addTag.isEmpty() && removeTag.isEmpty()) {
            throw new CheckedException("要添加与移除的标签不能同时为空");
        }
        WxCpService wxCpService = wxCorpApiStrategy.getWxCpService(req.getCorpId());
        try {
            wxCpService.getExternalContactService().markTag(
                    req.getUserId(), req.getExternalUserId(),
                    addTag.toArray(new String[0]), removeTag.toArray(new String[0]));
        } catch (WxErrorException e) {
            log.warn("[customer] 企微标签编辑失败：corpId={}, externalUserId={}, {}",
                    req.getCorpId(), req.getExternalUserId(), e.getMessage());
            throw new CheckedException("企微标签编辑失败：" + e.getMessage());
        }
        // 企微打标成功后刷新本地客户快照（extra_data 里的标签随之更新）
        syncCustomer(TenantContextHolder.getTenantId(), req.getCorpId(),
                SyncType.CUSTOMER_INCREMENT.getCode(), TriggerType.MANUAL.getCode(),
                req.getExternalUserId(), req.getUserId());
    }

    /**
     * 修改客户备注信息（企微 externalcontact/remark）。
     * 企微更新成功后刷新本地客户快照（extra_data 里的备注随之更新）。
     */
    @Override
    public void remark(WxCustomerRemarkReq req) {
        boolean allEmpty = !StringUtils.hasText(req.getRemark())
                && !StringUtils.hasText(req.getDescription())
                && !StringUtils.hasText(req.getRemarkCompany())
                && (req.getRemarkMobiles() == null || req.getRemarkMobiles().isEmpty());
        if (allEmpty) {
            throw new CheckedException("备注信息不能全部为空");
        }
        WxCpService wxCpService = wxCorpApiStrategy.getWxCpService(req.getCorpId());
        try {
            wxCpService.getExternalContactService().updateRemark(
                    me.chanjar.weixin.cp.bean.external.WxCpUpdateRemarkRequest.builder()
                            .userId(req.getUserId())
                            .externalUserId(req.getExternalUserId())
                            .remark(req.getRemark())
                            .description(req.getDescription())
                            .remarkCompany(req.getRemarkCompany())
                            .remarkMobiles(req.getRemarkMobiles() == null ? null
                                    : req.getRemarkMobiles().toArray(new String[0]))
                            .build());
        } catch (WxErrorException e) {
            log.warn("[customer] 企微备注修改失败：corpId={}, externalUserId={}, {}",
                    req.getCorpId(), req.getExternalUserId(), e.getMessage());
            throw new CheckedException("企微备注修改失败：" + e.getMessage());
        }
        // 企微更新成功后刷新本地客户快照（extra_data 里的备注随之更新）
        syncCustomer(TenantContextHolder.getTenantId(), req.getCorpId(),
                SyncType.CUSTOMER_INCREMENT.getCode(), TriggerType.MANUAL.getCode(),
                req.getExternalUserId(), req.getUserId());
    }

    /**
     * 交接回填：客户被原成员解除关系（24 小时内标记为"员工删除客户"的流失）
     * 后又被新成员添加，属于客户交接而非流失，改写流失原因以便与真实流失区分。
     */
    private void backfillHandoverReason(String corpId, String externalUserId, String newUserId) {
        WxUser newMember = wxUserMapper.selectOne(Wrappers.<WxUser>lambdaQuery()
                .eq(WxUser::getCorpId, corpId)
                .eq(WxUser::getUserId, newUserId)
                .last("LIMIT 1"));
        String newMemberName = newMember != null && StringUtils.hasText(newMember.getName())
                ? newMember.getName()
                : newUserId;
        int updated = wxCustomerMapper.update(null, Wrappers.<WxCustomer>lambdaUpdate()
                .eq(WxCustomer::getCorpId, corpId)
                .eq(WxCustomer::getExternalUserId, externalUserId)
                .ne(WxCustomer::getUserId, newUserId)
                .eq(WxCustomer::getLostFlag, 1)
                .eq(WxCustomer::getLostBy, LOST_BY_EMPLOYEE)
                .ge(WxCustomer::getLostTime, LocalDateTime.now().minusHours(24))
                .set(WxCustomer::getLostReason, "客户交接给成员 " + newMemberName)
                .set(WxCustomer::getHandoverToUserid, newUserId));
        if (updated > 0) {
            log.info("[customer] 客户交接回填完成：corpId={}, externalUserId={}, 新成员={}, rows={}",
                    corpId, externalUserId, newMemberName, updated);
        }
    }

    /**
     * 客户流失（好友关系解除，不做软删除）。
     *
     * <p>del_external_contact=客户删除成员；del_follow_user=员工删除客户
     * （含成员离职/禁用场景）。仅打流失标记（发起方/原因/时间）并推进
     * update_time，客户重新添加时由 batchUpsert 按 update_time 门控
     * 清除流失标记完成恢复。
     */
    @Override
    public void syncDelCustomer(Long tenantId, String corpId, String syncType, String triggerType, String externalUserId, String userId, int lostBy) {
        // 员工删除：跟进成员已离职（不在通讯录或退出企业）时，关系解除只可能来自继承流程，原因需区分
        String reason = "员工删除客户";
        if (lostBy == LOST_BY_EMPLOYEE) {
            WxUser member = wxUserMapper.selectOne(Wrappers.<WxUser>lambdaQuery()
                    .eq(WxUser::getCorpId, corpId)
                    .eq(WxUser::getUserId, userId)
                    .last("LIMIT 1"));
            boolean departed = member == null
                    || member.getStatus() == null
                    || member.getStatus() == 5;
            reason = departed ? "原跟进成员已离职，跟进关系解除" : "员工删除客户";
        }
        wxCustomerMapper.update(null, Wrappers.<WxCustomer>lambdaUpdate()
                .eq(WxCustomer::getCorpId, corpId)
                .eq(WxCustomer::getExternalUserId, externalUserId)
                .eq(WxCustomer::getUserId, userId)
                .set(WxCustomer::getLostFlag, 1)
                .set(WxCustomer::getLostBy, lostBy)
                .set(WxCustomer::getLostReason, reason)
                .set(WxCustomer::getLostTime, LocalDateTime.now())
                .set(WxCustomer::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    public void syncInheritTransferLost(Long tenantId, String corpId, String externalUserId) {
        wxCustomerMapper.update(null, Wrappers.<WxCustomer>lambdaUpdate()
                .eq(WxCustomer::getCorpId, corpId)
                .eq(WxCustomer::getExternalUserId, externalUserId)
                .eq(WxCustomer::getLostFlag, 0)
                .set(WxCustomer::getLostFlag, 1)
                .set(WxCustomer::getLostBy, LOST_BY_INHERIT_TRANSFER)
                .set(WxCustomer::getLostReason, "在职继承自动转接，客户已转接至新成员")
                .set(WxCustomer::getLostTime, LocalDateTime.now())
                .set(WxCustomer::getUpdateTime, LocalDateTime.now()));
        log.info("[customer] 在职继承自动转接解除原跟进关系：corpId={}, externalUserId={}", corpId, externalUserId);
    }

    /**
     * 企微接替失败原因 → 流失原因文案
     */
    private String resolveTransferFailReason(String failReason) {
        return switch (failReason == null ? "" : failReason) {
            case "customer_refused" -> "客户拒绝接替";
            case "customer_limit_exceed" -> "接替成员客户数达上限";
            default -> "客户接替失败：" + failReason;
        };
    }

    @Override
    public void syncTransferFail(Long tenantId, String corpId, String externalUserId, String failReason) {
        // 客户拒绝接替：原跟进关系保持不变（在职继承场景原成员仍在跟进），不打流失标记；
        // 并清除发起接替时留下的交接注释（拒绝=不交接了）
        if ("customer_refused".equals(failReason)) {
            wxCustomerMapper.update(null, Wrappers.<WxCustomer>lambdaUpdate()
                    .eq(WxCustomer::getCorpId, corpId)
                    .eq(WxCustomer::getExternalUserId, externalUserId)
                    .eq(WxCustomer::getLostFlag, 0)
                    .like(WxCustomer::getLostReason, "已发起")
                    .set(WxCustomer::getLostReason, null)
                    .set(WxCustomer::getUpdateTime, LocalDateTime.now()));
            log.info("[customer] 客户拒绝接替，保持原跟进关系：corpId={}, externalUserId={}", corpId, externalUserId);
            return;
        }
        // 其他失败（如接替成员客户数达上限）：客户已悬空在离职成员名下，
        // 对该客户名下、跟进成员已离职/不存在于通讯录的关系行打流失标记
        List<WxCustomer> relations = wxCustomerMapper.selectList(
                Wrappers.<WxCustomer>lambdaQuery()
                        .eq(WxCustomer::getCorpId, corpId)
                        .eq(WxCustomer::getExternalUserId, externalUserId)
                        .eq(WxCustomer::getLostFlag, 0));
        if (relations.isEmpty()) {
            log.info("[customer] 接替失败但客户无待处理关系行，跳过：corpId={}, externalUserId={}", corpId, externalUserId);
            return;
        }
        List<Long> lostIds = new ArrayList<>();
        for (WxCustomer relation : relations) {
            WxUser member = wxUserMapper.selectOne(Wrappers.<WxUser>lambdaQuery()
                    .eq(WxUser::getCorpId, corpId)
                    .eq(WxUser::getUserId, relation.getUserId())
                    .last("LIMIT 1"));
            // 成员已不在通讯录，或状态为退出企业（5）视为已离职
            boolean departed = member == null
                    || member.getStatus() == null
                    || member.getStatus() == 5;
            if (departed) {
                lostIds.add(relation.getId());
            }
        }
        if (lostIds.isEmpty()) {
            log.info("[customer] 接替失败的客户仍有在职跟进成员，不打流失标记：corpId={}, externalUserId={}", corpId, externalUserId);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        wxCustomerMapper.update(null, Wrappers.<WxCustomer>lambdaUpdate()
                .in(WxCustomer::getId, lostIds)
                .set(WxCustomer::getLostFlag, 1)
                .set(WxCustomer::getLostBy, LOST_BY_TRANSFER_FAIL)
                .set(WxCustomer::getLostReason, resolveTransferFailReason(failReason))
                .set(WxCustomer::getLostTime, now)
                .set(WxCustomer::getUpdateTime, now));
        log.info("[customer] 客户接替失败已标记流失：corpId={}, externalUserId={}, rows={}",
                corpId, externalUserId, lostIds.size());
    }

    /**
     * 批量同步客户数据
     */
    private void syncCustomers(Long tenantId,String corpId, List<WxCpExternalContactBatchInfo.ExternalContactInfo> externalContactList, Long version) {
        if (externalContactList == null || externalContactList.isEmpty()) return;

        List<WxCustomer> list = externalContactList.stream().map(dto -> {
            WxCustomer cust = BeanUtil.copyProperties(dto.getExternalContact(), WxCustomer.class);
            cust.setId(null);
            cust.setCorpId(corpId);
            cust.setSyncVersion(version);
            // 将企微返回的完整 DTO 转为 Map 存入快照字段
            cust.setExtraData(BeanUtil.beanToMap(dto));
            cust.setTenantId(tenantId);
            cust.setUserId(dto.getFollowInfo().getUserId());
            // 该成员添加此外部联系人的时间（秒级时间戳）
            if (dto.getFollowInfo().getCreateTime() != null) {
                cust.setFollowCreateTime(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(dto.getFollowInfo().getCreateTime()), ZoneId.systemDefault()));
            }
            // 渠道来源标识（联系我/获客链接的 state 参数）
            cust.setFollowState(dto.getFollowInfo().getState());
            cust.setCreateTime(LocalDateTime.now());
            cust.setUpdateTime(LocalDateTime.now());
            cust.setDelFlag(0);
            // 全量同步以企微最新数据为准：清除流失标记
            cust.setLostFlag(0);
            return cust;
        }).collect(Collectors.toList());

        // 批量 Upsert
        wxCustomerMapper.batchUpsert(list);
    }

}
