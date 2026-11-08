package com.bill.portal.scrm.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bill.portal.scrm.common.entity.WxSyncVersion;

public interface ISyncVersionService extends IService<WxSyncVersion> {

    /**
     * 生成全局递增版本号
     * 事务保证：版本号递增与日志记录在同一事务中，避免 Redis 方案可能产生的版本号空洞问题
     */
    Long generateNewVersion(String corpId,String syncType, String triggerType);

    /**
     * 更新同步状态
     */
    void updateVersionStatus(String corpId, String syncType, Long versionNo, String status, String remark);
}
