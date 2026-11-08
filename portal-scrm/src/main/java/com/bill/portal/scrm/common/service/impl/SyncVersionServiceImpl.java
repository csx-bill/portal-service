package com.bill.portal.scrm.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bill.portal.common.core.tenant.TenantContextHolder;
import com.bill.portal.scrm.common.entity.WxSyncVersion;
import com.bill.portal.scrm.common.mapper.WxSyncVersionMapper;
import com.bill.portal.scrm.common.service.ISyncVersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 版本号控制服务
 * 核心设计：纯 MySQL 实现，天然环境隔离，基于行锁保证并发安全
 */
@Slf4j
@Service
public class SyncVersionServiceImpl extends ServiceImpl<WxSyncVersionMapper, WxSyncVersion> implements ISyncVersionService {

    /**
     * 生成指定企业的同步版本号
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long generateNewVersion(String corpId, String syncType, String triggerType) {
        // 1. 查询当前租户当前企业该类型下最大的 version_no
        // 使用 FOR UPDATE 锁定该记录（如果存在），防止并发查询导致 version_no 重复
        Long maxVersionNo = baseMapper.selectMaxVersionNoForUpdate(corpId);

        long nextVersionNo = (maxVersionNo == null) ? 1L : maxVersionNo + 1;

        // 2. 插入一条新的同步流水记录
        WxSyncVersion newVersion = new WxSyncVersion();
        newVersion.setCorpId(corpId);
        newVersion.setSyncType(syncType);
        newVersion.setTriggerType(triggerType);
        newVersion.setVersionNo(nextVersionNo); // 设置递增后的版本号
        newVersion.setStatus("RUNNING");
        newVersion.setStartTime(LocalDateTime.now());

        // 如果并发极高，依赖唯一索引 uk_tenant_corp_sync_version 做最终防线
        try {
            this.save(newVersion);
        } catch (DuplicateKeyException e) {
            // 理论上 FOR UPDATE 已经防住，若极端情况出现唯一索引冲突，说明号被抢，抛出异常或重试
            log.error("租户[{}]企业[{}]版本号并发冲突，生成失败", TenantContextHolder.getTenantId(), corpId, e);
            throw new RuntimeException("系统繁忙，版本号生成冲突，请稍后重试");
        }

        log.info("企业[{},{}]生成同步版本号: {}", corpId,syncType, nextVersionNo);
        return nextVersionNo;
    }

    /**
     * 更新同步状态
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateVersionStatus(String corpId,String syncType ,Long versionNo, String status, String remark) {
        LambdaUpdateWrapper<WxSyncVersion> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(WxSyncVersion::getCorpId, corpId)
                .eq(WxSyncVersion::getSyncType, syncType)
                .eq(WxSyncVersion::getVersionNo, versionNo)
                .set(WxSyncVersion::getStatus, status)
                .set(WxSyncVersion::getEndTime, LocalDateTime.now())
                .set(WxSyncVersion::getRemark, remark);
        baseMapper.update(new WxSyncVersion(), wrapper);
    }
}
