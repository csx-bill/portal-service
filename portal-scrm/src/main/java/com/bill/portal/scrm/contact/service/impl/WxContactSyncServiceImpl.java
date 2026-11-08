package com.bill.portal.scrm.contact.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bill.portal.scrm.contact.entity.WxDepartment;
import com.bill.portal.scrm.contact.entity.WxUser;
import com.bill.portal.scrm.contact.mapper.WxDepartmentMapper;
import com.bill.portal.scrm.contact.mapper.WxUserMapper;
import com.bill.portal.scrm.common.service.ISyncVersionService;
import com.bill.portal.scrm.contact.service.IWxContactSyncService;
import com.bill.portal.scrm.corp.strategy.WxCorpApiStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.api.WxCpDepartmentService;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.api.WxCpUserService;
import me.chanjar.weixin.cp.bean.WxCpDepart;
import me.chanjar.weixin.cp.bean.WxCpUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 通讯录同步核心服务
 */
@Slf4j
@Service
@AllArgsConstructor
public class WxContactSyncServiceImpl implements IWxContactSyncService {

    private final WxCorpApiStrategy wxCorpApiStrategy;

    private final WxUserMapper wxUserMapper;

    private final WxDepartmentMapper wxDepartmentMapper;

    private final ISyncVersionService versionService;

    /**
     * 执行全量同步
     * 大组织性能优化：按部门维度并发拉取用户 + 批量 Upsert
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
            WxCpDepartmentService departmentService = wxCpService.getDepartmentService();

            // 1. 获取全量部门列表
            List<WxCpDepart> departments = departmentService.list(null);

            // 2. 同步部门数据到数据库
            syncDepartments(tenantId,corpId, departments, currentVersion);

            // 3. 提取部门ID列表，用于拉取用户
            List<Long> deptIds = departments.stream()
                    .map(WxCpDepart::getId)
                    .collect(Collectors.toList());

            // 4. 按部门拉取用户
            for (Long deptId : deptIds) {
                syncDeptUsers(tenantId, corpId, deptId, currentVersion);
            }
            versionService.updateVersionStatus(corpId, syncType, currentVersion, "SUCCESS", null);
            log.info("企业[{}]全量同步完成, version: {}", corpId, currentVersion);
        } catch (Exception e) {
            log.error("企业[{}]全量同步失败, version: {}", corpId, currentVersion, e);
            versionService.updateVersionStatus(corpId, syncType, currentVersion, "FAIL", e.getMessage());
        }
    }

    /**
     * 同步成员信息
     * @param tenantId 租户ID
     * @param corpId
     * @param syncType 同步类型
     * @param triggerType 触发类型
     * @param userId 用户ID
     */
    @Override
    public void syncUser(Long tenantId, String corpId, String syncType, String triggerType, String userId) {
        WxCpService wxCpService = wxCorpApiStrategy.getWxCpService(corpId);
        // 2. 生成增量版本号 (与后续DB操作同事务，保证一致性)
        Long currentVersion = versionService.generateNewVersion(corpId, syncType, triggerType);
        try {
            WxCpUser dto = wxCpService.getUserService().getById(userId);

            WxUser user = BeanUtil.copyProperties(dto, WxUser.class);
            user.setCorpId(corpId);
            user.setSyncVersion(currentVersion);
            // 将企微返回的完整 DTO 转为 Map 存入快照字段
            user.setExtraData(BeanUtil.beanToMap(dto));
            user.setTenantId(tenantId);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());

            wxUserMapper.batchUpsert(Arrays.asList(user));
            versionService.updateVersionStatus(corpId, syncType, currentVersion, "SUCCESS", null);
        }catch (Exception e){
            versionService.updateVersionStatus(corpId, syncType, currentVersion, "FAIL", e.getMessage());
        }

    }

    /**
     * 删除成员信息
     * @param tenantId 租户ID
     * @param corpId
     * @param syncType 同步类型
     * @param triggerType 触发类型
     * @param userId 用户ID
     */
    @Override
    public void syncDelUser(Long tenantId, String corpId, String syncType, String triggerType, String userId) {
        wxUserMapper.delete(Wrappers.<WxUser>lambdaQuery().eq(WxUser::getCorpId,corpId).eq(WxUser::getUserId,userId));
    }

    /**
     * 同步部门信息
     * @param tenantId 租户ID
     * @param corpId
     * @param syncType 同步类型
     * @param triggerType 触发类型
     * @param deptId 部门ID
     */
    @Override
    public void syncDept(Long tenantId, String corpId, String syncType, String triggerType, Long deptId) {
        WxCpService wxCpService = wxCorpApiStrategy.getWxCpService(corpId);
        // 2. 生成增量版本号 (与后续DB操作同事务，保证一致性)
        Long currentVersion = versionService.generateNewVersion(corpId, syncType, triggerType);
        try {
            WxCpDepart dto = wxCpService.getDepartmentService().get(deptId);

            WxDepartment dept = BeanUtil.copyProperties(dto, WxDepartment.class);
            dept.setId(null);
            dept.setCorpId(corpId);
            dept.setDeptId(dto.getId());
            dept.setSyncVersion(currentVersion);
            dept.setTenantId(tenantId);
            dept.setCreateTime(LocalDateTime.now());
            dept.setUpdateTime(LocalDateTime.now());

            // 批量 Upsert 部门
            wxDepartmentMapper.batchUpsert(Arrays.asList(dept));
            versionService.updateVersionStatus(corpId, syncType, currentVersion, "SUCCESS", null);
        }catch (Exception e){
            versionService.updateVersionStatus(corpId, syncType, currentVersion, "FAIL", e.getMessage());
        }

    }


    /**
     * 删除部门信息
     * @param tenantId 租户ID
     * @param corpId
     * @param syncType 同步类型
     * @param triggerType 触发类型
     * @param deptId 部门ID
     */
    @Override
    public void syncDelDept(Long tenantId, String corpId, String syncType, String triggerType, Long deptId) {
        wxDepartmentMapper.delete(Wrappers.<WxDepartment>lambdaQuery().eq(WxDepartment::getCorpId,corpId).eq(WxDepartment::getDeptId,deptId));
    }

    /**
     * 批量同步部门数据
     */
    private void syncDepartments(Long tenantId,String corpId, List<WxCpDepart> departments, Long version) {
        if (departments == null || departments.isEmpty()) return;

        List<WxDepartment> list = departments.stream().map(dto -> {
            WxDepartment dept = BeanUtil.copyProperties(dto, WxDepartment.class);
            dept.setId(null);
            dept.setCorpId(corpId);
            dept.setDeptId(dto.getId());
            dept.setSyncVersion(version);
            dept.setTenantId(tenantId);
            dept.setCreateTime(LocalDateTime.now());
            dept.setUpdateTime(LocalDateTime.now());
            return dept;
        }).collect(Collectors.toList());

        // 批量 Upsert 部门
        wxDepartmentMapper.batchUpsert(list);
    }

    /**
     * 并发拉取部门用户并批量入库
     */
    private void syncDeptUsers(Long tenantId,String corpId, Long deptId, Long version) {
        try {
            WxCpService wxCpService = wxCorpApiStrategy.getWxCpService(corpId);
            WxCpUserService userService = wxCpService.getUserService();
            List<WxCpUser> wxUsers = userService.listByDepartment(deptId, false, null);

            if (wxUsers == null || wxUsers.isEmpty()) return;

            List<WxUser> list = wxUsers.stream().map(dto -> {
                WxUser user = BeanUtil.copyProperties(dto, WxUser.class);
                user.setCorpId(corpId);
                user.setSyncVersion(version);
                // 将企微返回的完整 DTO 转为 Map 存入快照字段
                user.setExtraData(BeanUtil.beanToMap(dto));
                user.setTenantId(tenantId);
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(LocalDateTime.now());
                return user;
            }).collect(Collectors.toList());

            wxUserMapper.batchUpsert(list);
        } catch (Exception e) {
            log.error("企业[{}]拉取部门[{}]用户失败", corpId, deptId, e);
            throw new RuntimeException("拉取部门用户失败", e);
        }
    }

}
