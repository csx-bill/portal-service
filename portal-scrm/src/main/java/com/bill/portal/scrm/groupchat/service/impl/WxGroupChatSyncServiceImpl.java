package com.bill.portal.scrm.groupchat.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bill.portal.scrm.common.service.ISyncVersionService;
import com.bill.portal.scrm.corp.strategy.WxCorpApiStrategy;
import com.bill.portal.scrm.groupchat.entity.WxGroupChat;
import com.bill.portal.scrm.groupchat.entity.WxGroupChatMember;
import com.bill.portal.scrm.groupchat.entity.WxGroupChatMemberLog;
import com.bill.portal.scrm.groupchat.mapper.WxGroupChatMapper;
import com.bill.portal.scrm.groupchat.mapper.WxGroupChatMemberMapper;
import com.bill.portal.scrm.groupchat.mapper.WxGroupChatMemberLogMapper;
import com.bill.portal.scrm.groupchat.service.IWxGroupChatSyncService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.external.WxCpUserExternalGroupChatInfo;
import me.chanjar.weixin.cp.bean.external.WxCpUserExternalGroupChatList;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 企微客户群同步核心服务
 */
@Slf4j
@Service
@AllArgsConstructor
public class WxGroupChatSyncServiceImpl implements IWxGroupChatSyncService {

    private final WxCorpApiStrategy wxCorpApiStrategy;

    private final WxGroupChatMapper wxGroupChatMapper;

    private final WxGroupChatMemberMapper wxGroupChatMemberMapper;

    private final WxGroupChatMemberLogMapper wxGroupChatMemberLogMapper;

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

            String cursor = null;
            int limit = 500;

            // 1. 循环拉取群列表
            do {
                WxCpUserExternalGroupChatList chatList = wxCpService.getExternalContactService().listGroupChat(limit, cursor, 0, null);
                if (chatList == null || CollectionUtils.isEmpty(chatList.getGroupChatList())) {
                    break;
                }
                // 同步群
                syncGroupChats(tenantId,corpId,chatList.getGroupChatList(),currentVersion);

                cursor = chatList.getNextCursor();

            } while (cursor != null);

            versionService.updateVersionStatus(corpId, syncType, currentVersion, "SUCCESS", null);
            log.info("企业[{}]全量同步完成, version: {}", corpId, currentVersion);
        } catch (Exception e) {
            log.error("企业[{}]全量同步失败, version: {}", corpId, currentVersion, e);
            versionService.updateVersionStatus(corpId, syncType, currentVersion, "FAIL", e.getMessage());
        }
    }

    @Override
    public void syncGroupChat(Long tenantId, String corpId, String syncType, String triggerType, String chatId,
                              String updateDetail, Long lastMemVer, Long curMemVer) {
        // 1. 生成增量版本号 (与后续DB操作同事务，保证一致性)
        Long currentVersion = versionService.generateNewVersion(corpId, syncType, triggerType);

        try {
            // 2. 成员版本一致性校验（自建应用回调携带版本号）：事件 LastMemVer 与本地
            //    最新版本不一致，说明事件丢失或乱序，log 告警（详情重拉本身即为兜底）
            WxGroupChat existing = wxGroupChatMapper.selectOne(Wrappers.<WxGroupChat>lambdaQuery()
                    .eq(WxGroupChat::getCorpId, corpId)
                    .eq(WxGroupChat::getChatId, chatId)
                    .last("LIMIT 1"));
            if (lastMemVer != null && existing != null && existing.getMemberVersion() != null
                    && !lastMemVer.equals(existing.getMemberVersion())) {
                log.warn("[groupchat] 成员变更事件版本不一致（疑似事件丢失/乱序），已按详情重拉兜底：" +
                        "chatId={}, 事件LastMemVer={}, 本地版本={}", chatId, lastMemVer, existing.getMemberVersion());
            }

            List<WxCpUserExternalGroupChatList.ChatStatus> groupChatList = new ArrayList<>();
            WxCpUserExternalGroupChatList.ChatStatus chatStatus = new WxCpUserExternalGroupChatList.ChatStatus();
            chatStatus.setChatId(chatId);
            chatStatus.setStatus(0);
            syncGroupChats(tenantId,corpId,groupChatList,currentVersion);
            // 记录最新群成员版本号（本地一致性校验字段）
            if (curMemVer != null) {
                wxGroupChatMapper.update(null, Wrappers.<WxGroupChat>lambdaUpdate()
                        .eq(WxGroupChat::getCorpId, corpId)
                        .eq(WxGroupChat::getChatId, chatId)
                        .set(WxGroupChat::getMemberVersion, curMemVer));
            }
            versionService.updateVersionStatus(corpId, syncType, currentVersion, "SUCCESS", null);
        }catch (Exception e){
            versionService.updateVersionStatus(corpId, syncType, currentVersion, "FAIL", e.getMessage());
        }

    }

    @Override
    public void syncDismissGroupChat(Long tenantId, String corpId, String syncType, String triggerType, String chatId) {
        // 群解散为真实业务状态：群与成员记录保留，打解散标记供列表标识
        wxGroupChatMapper.update(null, Wrappers.<WxGroupChat>lambdaUpdate()
                .eq(WxGroupChat::getCorpId, corpId)
                .eq(WxGroupChat::getChatId, chatId)
                .set(WxGroupChat::getDismissFlag, 1)
                .set(WxGroupChat::getDismissTime, LocalDateTime.now())
                .set(WxGroupChat::getUpdateTime, LocalDateTime.now()));
        log.info("[groupchat] 客户群已解散并标记：corpId={}, chatId={}", corpId, chatId);
    }


    /**
     * 批量同步客户群
     */
    private void syncGroupChats(Long tenantId,String corpId, List<WxCpUserExternalGroupChatList.ChatStatus> groupChatList, Long version) throws WxErrorException {
        if (groupChatList == null || groupChatList.isEmpty()) return;
        WxCpService wxCpService = wxCorpApiStrategy.getWxCpService(corpId);

        List<WxGroupChat> list = new ArrayList<>();
        // 遍历群ID，拉取群详情并落库
        for (WxCpUserExternalGroupChatList.ChatStatus chatStatus : groupChatList) {
            WxCpUserExternalGroupChatInfo chatInfo = wxCpService.getExternalContactService().getGroupChat(chatStatus.getChatId(), 1);
            WxCpUserExternalGroupChatInfo.GroupChat groupChat = chatInfo.getGroupChat();
            List<String> adminList = groupChat.getAdminList().stream().map(WxCpUserExternalGroupChatInfo.GroupAdmin::getUserId).toList();

            WxGroupChat wxGroupChat = new WxGroupChat();
            wxGroupChat.setChatId(chatStatus.getChatId());
            wxGroupChat.setStatus(chatStatus.getStatus());
            wxGroupChat.setChatName(groupChat.getName());
            // 群创建时间（企微群详情返回的秒级时间戳）
            if (groupChat.getCreateTime() != null) {
                wxGroupChat.setGroupCreateTime(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(groupChat.getCreateTime()), ZoneId.systemDefault()));
            }
            wxGroupChat.setNotice(groupChat.getNotice());
            wxGroupChat.setOwner(groupChat.getOwner());
            wxGroupChat.setAdminList(adminList);
            wxGroupChat.setMemberNum(groupChat.getMemberList().size());
            wxGroupChat.setCorpId(corpId);
            wxGroupChat.setSyncVersion(version);
            wxGroupChat.setTenantId(tenantId);
            wxGroupChat.setCreateTime(LocalDateTime.now());
            wxGroupChat.setUpdateTime(LocalDateTime.now());

            list.add(wxGroupChat);
            
            // 同步群成员信息
            syncGroupChatMember(tenantId,corpId,groupChat.getMemberList(),version,chatStatus.getChatId());
        }

        // 批量 Upsert
        wxGroupChatMapper.batchUpsert(list);
    }


    /**
     * 批量同步群成员（diff 记录入群/退群变动流水，用于群统计）
     */
    private void syncGroupChatMember(Long tenantId, String corpId, List<WxCpUserExternalGroupChatInfo.GroupMember> memberList, Long version,String chatId) {
        if (CollectionUtils.isEmpty(memberList)) return;

        // 现有成员行（用于 diff 出入群/退群）
        List<WxGroupChatMember> existing = wxGroupChatMemberMapper.selectList(
                Wrappers.<WxGroupChatMember>lambdaQuery()
                        .eq(WxGroupChatMember::getCorpId, corpId)
                        .eq(WxGroupChatMember::getChatId, chatId));

        List<WxGroupChatMember> list = memberList.stream().map(dto -> {
            WxGroupChatMember member = new WxGroupChatMember();
            member.setChatId(chatId);
            member.setUserId(dto.getUserId());
            member.setType(dto.getType());
            member.setUnionId(dto.getUnionId());
            member.setJoinTime(LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(dto.getJoinTime()),
                    ZoneId.systemDefault()
            ));
            member.setJoinScene(dto.getJoinScene());
            if(Objects.nonNull(dto.getInvitor())){
                member.setInvitorUserId(dto.getInvitor().getUserId());
            }
            member.setGroupNickname(dto.getGroupNickname());
            member.setName(dto.getName());

            member.setCorpId(corpId);
            member.setSyncVersion(version);
            member.setTenantId(tenantId);
            member.setCreateTime(LocalDateTime.now());
            member.setUpdateTime(LocalDateTime.now());
            member.setDelFlag(0);
            return member;
        }).collect(Collectors.toList());

        // 批量 Upsert
        wxGroupChatMemberMapper.batchUpsert(list);

        // 变动流水：入群（当前有/原有无）、退群（原有有/当前无）
        Set<String> existingUserIds = existing.stream()
                .map(WxGroupChatMember::getUserId).collect(Collectors.toSet());
        Set<String> currentUserIds = list.stream()
                .map(WxGroupChatMember::getUserId).collect(Collectors.toSet());

        List<WxGroupChatMemberLog> logs = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (WxGroupChatMember member : list) {
            if (member.getUserId() == null || existingUserIds.contains(member.getUserId())) {
                continue;
            }
            logs.add(buildMemberLog(corpId, chatId, member.getUserId(), member.getName(),
                    1, member.getJoinTime() != null ? member.getJoinTime() : now, version, tenantId, now));
        }
        for (WxGroupChatMember member : existing) {
            if (member.getUserId() == null || currentUserIds.contains(member.getUserId())) {
                continue;
            }
            logs.add(buildMemberLog(corpId, chatId, member.getUserId(), member.getName(),
                    2, now, version, tenantId, now));
        }
        logs.forEach(wxGroupChatMemberLogMapper::insert);
        if (!logs.isEmpty()) {
            log.info("[groupchat] 群成员变动记录：chatId={}, 入群={}，退群={}", chatId,
                    logs.stream().filter(l -> Integer.valueOf(1).equals(l.getChangeType())).count(),
                    logs.stream().filter(l -> Integer.valueOf(2).equals(l.getChangeType())).count());
        }

        // 移除已退群成员行（退群流水已记录；不删则下次同步会重复记退群）
        List<String> departedUserIds = existing.stream()
                .map(WxGroupChatMember::getUserId)
                .filter(id -> id != null && !currentUserIds.contains(id))
                .collect(Collectors.toList());
        if (!departedUserIds.isEmpty()) {
            wxGroupChatMemberMapper.delete(Wrappers.<WxGroupChatMember>lambdaQuery()
                    .eq(WxGroupChatMember::getCorpId, corpId)
                    .eq(WxGroupChatMember::getChatId, chatId)
                    .in(WxGroupChatMember::getUserId, departedUserIds));
        }
    }

    private WxGroupChatMemberLog buildMemberLog(String corpId, String chatId, String userid,
                                                String name, int changeType, LocalDateTime changeTime,
                                                Long version, Long tenantId, LocalDateTime now) {
        WxGroupChatMemberLog memberLog = new WxGroupChatMemberLog();
        memberLog.setCorpId(corpId);
        memberLog.setChatId(chatId);
        memberLog.setUserid(userid);
        memberLog.setName(name);
        memberLog.setChangeType(changeType);
        memberLog.setChangeTime(changeTime);
        memberLog.setSyncVersion(version);
        memberLog.setTenantId(tenantId);
        memberLog.setCreateTime(now);
        return memberLog;
    }

}
