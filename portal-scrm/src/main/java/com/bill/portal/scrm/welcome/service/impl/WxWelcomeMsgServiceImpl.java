package com.bill.portal.scrm.welcome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.common.core.exception.CheckedException;
import com.bill.portal.common.core.tenant.TenantContextHolder;
import com.bill.portal.scrm.contact.entity.WxDepartment;
import com.bill.portal.scrm.contact.entity.WxUser;
import com.bill.portal.scrm.contact.mapper.WxDepartmentMapper;
import com.bill.portal.scrm.contact.mapper.WxUserMapper;
import com.bill.portal.scrm.corp.strategy.WxCorpApiStrategy;
import com.bill.portal.scrm.service.IWxMediaService;
import com.bill.portal.scrm.welcome.dto.WelcomeBindItem;
import com.bill.portal.scrm.welcome.dto.WelcomeContentItem;
import com.bill.portal.scrm.welcome.dto.WelcomeTimeItem;
import com.bill.portal.scrm.welcome.dto.WxWelcomeMsgResp;
import com.bill.portal.scrm.welcome.entity.WxWelcomeMsg;
import com.bill.portal.scrm.welcome.entity.WxWelcomeMsgBind;
import com.bill.portal.scrm.welcome.entity.WxWelcomeMsgRule;
import com.bill.portal.scrm.welcome.mapper.WxWelcomeMsgBindMapper;
import com.bill.portal.scrm.welcome.mapper.WxWelcomeMsgMapper;
import com.bill.portal.scrm.welcome.mapper.WxWelcomeMsgRuleMapper;
import com.bill.portal.scrm.welcome.req.WxWelcomeMsgQuery;
import com.bill.portal.scrm.welcome.req.WxWelcomeMsgSaveReq;
import com.bill.portal.scrm.welcome.service.IWxWelcomeMsgService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.bean.external.WxCpWelcomeMsg;
import me.chanjar.weixin.cp.bean.external.msg.Attachment;
import me.chanjar.weixin.cp.bean.external.msg.File;
import me.chanjar.weixin.cp.bean.external.msg.Image;
import me.chanjar.weixin.cp.bean.external.msg.Link;
import me.chanjar.weixin.cp.bean.external.msg.MiniProgram;
import me.chanjar.weixin.cp.bean.external.msg.Text;
import me.chanjar.weixin.cp.bean.external.msg.Video;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 企微欢迎语服务实现（欢迎语即分时规则列表）。
 *
 * <p>主表 wx_welcome_msg 仅作容器（标题/企业）；规则存
 * wx_welcome_msg_rule（内容 JSON + 生效星期/时间段 + 媒体素材缓存），
 * 整组替换；绑定按唯一键 (corp_id, target_type, target_id) 改指向；
 * 图片/视频内容项保存时按规则预上传企微临时素材并缓存 media_id
 * （约3天有效，过期或地址变更自动重传，失败不阻断保存）。
 */
@Slf4j
@Service
@AllArgsConstructor
public class WxWelcomeMsgServiceImpl implements IWxWelcomeMsgService {

    private static final Gson GSON = new Gson();
    private static final Type CONTENT_LIST_TYPE =
            new TypeToken<List<WelcomeContentItem>>() {}.getType();

    private static final int BIND_TARGET_USER = 1;
    private static final int BIND_TARGET_DEPT = 2;
    /** 跨欢迎语绑定冲突错误前缀（前端据此弹出改绑确认） */
    private static final String BIND_CONFLICT_PREFIX = "绑定冲突：";
    /** 企微临时素材有效期（官方为3天，留2小时余量触发提前重传） */
    private static final int MEDIA_EXPIRE_DAYS = 3;
    /** 欢迎语内容消息类型（与前端内容编辑器约定） */
    private static final String MSG_TEXT = "text";
    private static final String MSG_IMAGE = "image";
    private static final String MSG_VIDEO = "video";
    private static final String MSG_LINK = "link";
    private static final String MSG_FILE = "file";
    private static final String MSG_MINIPROGRAM = "miniprogram";

    private final WxWelcomeMsgMapper wxWelcomeMsgMapper;
    private final WxWelcomeMsgRuleMapper wxWelcomeMsgRuleMapper;
    private final WxWelcomeMsgBindMapper wxWelcomeMsgBindMapper;
    private final WxUserMapper wxUserMapper;
    private final WxDepartmentMapper wxDepartmentMapper;
    private final IWxMediaService wxMediaService;
    private final WxCorpApiStrategy wxCorpApiStrategy;

    @Override
    public IPage<WxWelcomeMsg> page(Page<WxWelcomeMsg> page, WxWelcomeMsgQuery query) {
        LambdaQueryWrapper<WxWelcomeMsg> queryWrapper =
                Wrappers.<WxWelcomeMsg>lambdaQuery()
                        .eq(StringUtils.hasText(query.getCorpId()), WxWelcomeMsg::getCorpId, query.getCorpId())
                        .like(StringUtils.hasText(query.getName()), WxWelcomeMsg::getName, query.getName())
                        .orderByDesc(WxWelcomeMsg::getCreateTime);

        // 通讯录树按绑定成员过滤：企微 userid → wx_user.id → 绑定表 target_id
        if (StringUtils.hasText(query.getSender()) && StringUtils.hasText(query.getCorpId())) {
            List<Long> bindUserIds = wxUserMapper.selectList(
                            Wrappers.<WxUser>lambdaQuery()
                                    .eq(WxUser::getCorpId, query.getCorpId())
                                    .eq(WxUser::getUserId, query.getSender()))
                    .stream()
                    .map(WxUser::getId)
                    .collect(Collectors.toList());
            if (bindUserIds.isEmpty()) {
                // 成员不存在则直接返回空页
                return new Page<>(page.getCurrent(), page.getSize());
            }
            String idCsv = bindUserIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            queryWrapper.exists("SELECT 1 FROM wx_welcome_msg_bind b WHERE b.welcome_msg_id = wx_welcome_msg.id"
                    + " AND b.target_type = " + BIND_TARGET_USER + " AND b.target_id IN (" + idCsv + ")");
        }
        // 通讯录树按绑定部门过滤
        if (query.getDeptId() != null) {
            queryWrapper.exists("SELECT 1 FROM wx_welcome_msg_bind b WHERE b.welcome_msg_id = wx_welcome_msg.id"
                            + " AND b.target_type = " + BIND_TARGET_DEPT + " AND b.target_id = {0}",
                    query.getDeptId());
        }

        IPage<WxWelcomeMsg> result = wxWelcomeMsgMapper.selectPage(page, queryWrapper);

        // 回填每条欢迎语的规则数量、绑定数量与绑定范围（对象名称）
        List<WxWelcomeMsg> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Object> ids = records.stream().map(WxWelcomeMsg::getId).collect(Collectors.toList());
            Map<Object, Long> bindCountMap = wxWelcomeMsgBindMapper.selectList(
                            Wrappers.<WxWelcomeMsgBind>lambdaQuery()
                                    .in(WxWelcomeMsgBind::getWelcomeMsgId, ids))
                    .stream()
                    .collect(Collectors.groupingBy(WxWelcomeMsgBind::getWelcomeMsgId, Collectors.counting()));
            Map<Object, Long> ruleCountMap = wxWelcomeMsgRuleMapper.selectList(
                            Wrappers.<WxWelcomeMsgRule>lambdaQuery()
                                    .in(WxWelcomeMsgRule::getWelcomeMsgId, ids))
                    .stream()
                    .collect(Collectors.groupingBy(WxWelcomeMsgRule::getWelcomeMsgId, Collectors.counting()));
            List<WxWelcomeMsgBind> binds = wxWelcomeMsgBindMapper.selectList(
                    Wrappers.<WxWelcomeMsgBind>lambdaQuery()
                            .in(WxWelcomeMsgBind::getWelcomeMsgId, ids)
                            .orderByAsc(WxWelcomeMsgBind::getTargetType)
                            .orderByAsc(WxWelcomeMsgBind::getTargetId));
            Map<Long, List<WxWelcomeMsgBind>> bindsByMsg = binds.stream()
                    .collect(Collectors.groupingBy(WxWelcomeMsgBind::getWelcomeMsgId));
            // 批量解析绑定对象名称（成员/部门）
            List<Long> userIds = binds.stream()
                    .filter(row -> row.getTargetType() == BIND_TARGET_USER)
                    .map(WxWelcomeMsgBind::getTargetId)
                    .distinct()
                    .collect(Collectors.toList());
            List<Long> deptIds = binds.stream()
                    .filter(row -> row.getTargetType() == BIND_TARGET_DEPT)
                    .map(WxWelcomeMsgBind::getTargetId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, String> userNames = userIds.isEmpty()
                    ? Map.of()
                    : wxUserMapper.selectBatchIds(userIds).stream()
                            .collect(Collectors.toMap(WxUser::getId,
                                    user -> StringUtils.hasText(user.getName())
                                            ? user.getName()
                                            : String.valueOf(user.getId())));
            Map<Long, String> deptNames = deptIds.isEmpty()
                    ? Map.of()
                    : wxDepartmentMapper.selectBatchIds(deptIds).stream()
                            .collect(Collectors.toMap(WxDepartment::getId,
                                    dept -> StringUtils.hasText(dept.getName())
                                            ? dept.getName()
                                            : String.valueOf(dept.getId())));
            records.forEach(record -> {
                Long id = record.getId();
                List<WxWelcomeMsgBind> msgBinds = bindsByMsg.getOrDefault(id, List.of());
                record.setBindCount(bindCountMap.getOrDefault(id, 0L));
                record.setRuleCount(ruleCountMap.getOrDefault(id, 0L));
                record.setBindNames(msgBinds.stream()
                        .map(row -> {
                            String name = row.getTargetType() == BIND_TARGET_USER
                                    ? userNames.get(row.getTargetId())
                                    : deptNames.get(row.getTargetId());
                            return StringUtils.hasText(name)
                                    ? name : String.valueOf(row.getTargetId());
                        })
                        .collect(Collectors.toList()));
            });
        }
        return result;
    }

    @Override
    public WxWelcomeMsgResp getWelcomeMsgById(Long id) {
        WxWelcomeMsg entity = wxWelcomeMsgMapper.selectById(id);
        if (entity == null) {
            throw new CheckedException("欢迎语不存在");
        }
        // 显式逐字段映射（主行为容器，内容全部在规则上）
        WxWelcomeMsgResp resp = new WxWelcomeMsgResp();
        resp.setId(entity.getId());
        resp.setCorpId(entity.getCorpId());
        resp.setName(entity.getName());
        resp.setCreateTime(entity.getCreateTime());

        // 分时规则
        resp.setTimeItems(rulesOf(id));

        // 绑定列表（回填绑定对象名称）
        List<WelcomeBindItem> binds = listBinds(entity.getCorpId(), id);
        resp.setBinds(binds);
        return resp;
    }

    @Override
    public List<String> checkBindConflicts(WxWelcomeMsgSaveReq req) {
        List<WelcomeTimeItem> currentRules =
                req.getTimeItems() == null ? List.of() : req.getTimeItems();
        List<WelcomeBindItem> newBinds =
                req.getBinds() == null ? List.of() : req.getBinds();
        List<String> conflicts = new ArrayList<>();
        for (WelcomeBindItem bind : newBinds) {
            for (WxWelcomeMsgBind row : otherBindsOfTarget(req.getCorpId(), bind, req.getId())) {
                if (rulesOverlap(currentRules, rulesOf(row.getWelcomeMsgId()))) {
                    conflicts.add(describeBindConflict(row));
                    break;
                }
            }
        }
        return conflicts;
    }

    @Override
    public void saveWelcomeMsg(WxWelcomeMsgSaveReq req) {
        validate(req);

        // 主行仅作容器
        WxWelcomeMsg entity = new WxWelcomeMsg();
        entity.setId(req.getId());
        entity.setCorpId(req.getCorpId());
        entity.setName(req.getName().trim());

        if (req.getId() != null) {
            wxWelcomeMsgMapper.updateById(entity);
        } else {
            wxWelcomeMsgMapper.insert(entity);
        }

        // 分时规则：整组替换；每条规则各自预上传媒体素材（失败不阻断保存）
        wxWelcomeMsgRuleMapper.delete(Wrappers.<WxWelcomeMsgRule>lambdaQuery()
                .eq(WxWelcomeMsgRule::getWelcomeMsgId, entity.getId()));
        List<WelcomeTimeItem> items = req.getTimeItems() == null
                ? List.of()
                : req.getTimeItems();
        for (int i = 0; i < items.size(); i++) {
            WelcomeTimeItem item = items.get(i);
            WxWelcomeMsgRule rule = new WxWelcomeMsgRule();
            rule.setCorpId(req.getCorpId());
            rule.setWelcomeMsgId(entity.getId());
            rule.setName(StringUtils.hasText(item.getName())
                    ? item.getName().trim() : entity.getName());
            rule.setContent(GSON.toJson(item.getContent() == null
                    ? List.of() : item.getContent()));
            rule.setEffectiveAt(item.getEffectiveAt());
            rule.setStartTime(item.getStartTime());
            rule.setEndTime(item.getEndTime());
            rule.setSort(i);
            fillMedia(rule, item.getContent());
            wxWelcomeMsgRuleMapper.insert(rule);
        }

        // 绑定：整组替换；目标已绑定的欢迎语与当前欢迎语分时规则时间重叠时，
        // 默认阻断并提示归属，forceBind 时改指向
        saveBinds(req, entity.getId(), Boolean.TRUE.equals(req.getForceBind()));
    }

    @Override
    public void delWelcomeMsgs(List<?> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        wxWelcomeMsgMapper.deleteBatchIds(idList);
        wxWelcomeMsgRuleMapper.delete(Wrappers.<WxWelcomeMsgRule>lambdaQuery()
                .in(WxWelcomeMsgRule::getWelcomeMsgId, idList));
        wxWelcomeMsgBindMapper.delete(Wrappers.<WxWelcomeMsgBind>lambdaQuery()
                .in(WxWelcomeMsgBind::getWelcomeMsgId, idList));
    }

    // ==================== 私有方法 ====================

    private void validate(WxWelcomeMsgSaveReq req) {
        if (!StringUtils.hasText(req.getCorpId())) {
            throw new CheckedException("企业不能为空");
        }
        if (!StringUtils.hasText(req.getName())) {
            throw new CheckedException("欢迎语标题不能为空");
        }
        // 欢迎语即分时规则列表：至少一条规则
        if (CollectionUtils.isEmpty(req.getTimeItems())) {
            throw new CheckedException("请至少添加一条分时规则");
        }
        for (WelcomeTimeItem item : req.getTimeItems()) {
            if (CollectionUtils.isEmpty(item.getEffectiveAt())) {
                throw new CheckedException("分时规则的生效星期不能为空");
            }
            if (item.getStartTime() == null || item.getEndTime() == null
                    || !item.getStartTime().isBefore(item.getEndTime())) {
                throw new CheckedException("分时规则的开始时间需早于结束时间");
            }
            if (CollectionUtils.isEmpty(item.getContent())) {
                throw new CheckedException("分时规则内容不能为空");
            }
            for (WelcomeContentItem contentItem : item.getContent()) {
                validateContentItem(contentItem);
            }
        }
        // 分时规则时间段不得重叠（同星期 + 时间区间相交即冲突）：
        // 全天生效与分时段是兜底关系、允许共存；两条全天生效彼此冲突
        for (int i = 0; i < req.getTimeItems().size(); i++) {
            for (int j = i + 1; j < req.getTimeItems().size(); j++) {
                WelcomeTimeItem a = req.getTimeItems().get(i);
                WelcomeTimeItem b = req.getTimeItems().get(j);
                if (isAllDayRule(a) != isAllDayRule(b)) {
                    continue;
                }
                boolean dayOverlap = a.getEffectiveAt().stream()
                        .anyMatch(b.getEffectiveAt()::contains);
                boolean timeOverlap =
                        a.getStartTime().isBefore(b.getEndTime())
                                && b.getStartTime().isBefore(a.getEndTime());
                if (dayOverlap && timeOverlap) {
                    throw new CheckedException("分时规则「" + a.getName()
                            + "」与「" + b.getName() + "」的时间段重叠，请调整生效星期或时间");
                }
            }
        }
    }

    /** 全天生效规则：生效星期覆盖周一至周日且时间为 00:00:00~23:59:59 */
    private boolean isAllDayRule(WelcomeTimeItem item) {
        Set<Integer> days = new HashSet<>(
                item.getEffectiveAt() == null ? List.of() : item.getEffectiveAt());
        return days.containsAll(List.of(1, 2, 3, 4, 5, 6, 7))
                && LocalTime.of(0, 0).equals(item.getStartTime())
                && LocalTime.of(23, 59, 59).equals(item.getEndTime());
    }

    private void validateContentItem(WelcomeContentItem item) {
        switch (item.getMsgType() == null ? "" : item.getMsgType()) {
            case "text" -> {
                if (!StringUtils.hasText(item.getTextContent())) {
                    throw new CheckedException("文本内容不能为空");
                }
                // 企微 send_welcome_msg：text.content 最长 4000 字节（UTF-8）
                if (byteLength(item.getTextContent()) > 4000) {
                    throw new CheckedException("文本内容超过 4000 字节限制（当前 "
                            + byteLength(item.getTextContent()) + " 字节）");
                }
            }
            case "image", "video" -> {
                if (!StringUtils.hasText(item.getUrl())) {
                    throw new CheckedException("媒体内容缺少资源地址");
                }
            }
            case "file" -> {
                if (!StringUtils.hasText(item.getUrl())) {
                    throw new CheckedException("文件内容缺少资源地址");
                }
            }
            case "miniprogram" -> {
                if (!StringUtils.hasText(item.getMpTitle())) {
                    throw new CheckedException("小程序内容需填写标题");
                }
                if (byteLength(item.getMpTitle()) > 64) {
                    throw new CheckedException("小程序标题超过 64 字节限制");
                }
                if (!StringUtils.hasText(item.getMpAppid())
                        || !StringUtils.hasText(item.getMpPage())) {
                    throw new CheckedException("小程序内容需填写 appid 与页面路径");
                }
                if (!StringUtils.hasText(item.getUrl())) {
                    throw new CheckedException("小程序内容需上传封面图");
                }
            }
            case "link" -> {
                if (!StringUtils.hasText(item.getLinkTitle())
                        || !StringUtils.hasText(item.getLinkUrl())) {
                    throw new CheckedException("链接内容需填写标题与跳转地址");
                }
                // 企微 send_welcome_msg：link.title 最长 128 字节，link.desc 最长 512 字节
                if (byteLength(item.getLinkTitle()) > 128) {
                    throw new CheckedException("链接标题超过 128 字节限制");
                }
                if (StringUtils.hasText(item.getLinkDesc())
                        && byteLength(item.getLinkDesc()) > 512) {
                    throw new CheckedException("链接描述超过 512 字节限制");
                }
            }
            default -> throw new CheckedException("不支持的内容类型：" + item.getMsgType());
        }
    }

    /** UTF-8 字节长度（企微按字节计限） */
    private int byteLength(String text) {
        return text == null ? 0 : text.getBytes(StandardCharsets.UTF_8).length;
    }

    /** 按 UTF-8 字节截断（不截断多字节字符），用于发送前的防御性处理 */
    private String utf8Truncate(String text, int maxBytes) {
        if (byteLength(text) <= maxBytes) {
            return text;
        }
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        int end = maxBytes;
        // 回退到完整字符边界（跳过 UTF-8 续字节 10xxxxxx）
        while (end > 0 && (bytes[end] & 0xC0) == 0x80) {
            end--;
        }
        return new String(bytes, 0, end, StandardCharsets.UTF_8);
    }

    /** 保存时按规则预上传图片/视频到企微临时素材并缓存 media_id（失败仅告警） */
    private void fillMedia(WxWelcomeMsgRule rule, List<WelcomeContentItem> content) {
        Optional<WelcomeContentItem> mediaOpt = (content == null ? List.<WelcomeContentItem>of() : content)
                .stream()
                .filter(item -> "image".equals(item.getMsgType())
                        || "video".equals(item.getMsgType()) || "file".equals(item.getMsgType()))
                .filter(item -> StringUtils.hasText(item.getUrl()))
                .findFirst();
        if (mediaOpt.isEmpty()) {
            return;
        }
        WelcomeContentItem media = mediaOpt.get();
        boolean needUpload = !StringUtils.hasText(rule.getMediaId())
                || !StringUtils.hasText(rule.getMediaUrl())
                || !media.getUrl().equals(rule.getMediaUrl())
                || rule.getMediaExpiresAt() == null
                || rule.getMediaExpiresAt().isBefore(LocalDateTime.now().plusHours(2));
        if (!needUpload) {
            // 地址未变且 media_id 未过期：仅保留原值
            rule.setMediaUrl(media.getUrl());
            return;
        }
        try {
            String filename = StringUtils.hasText(media.getFilename())
                    ? media.getFilename()
                    : deriveFilename(media.getUrl());
            String mediaId = wxMediaService.uploadTmpMedia(
                    rule.getCorpId(), media.getMsgType(), filename, media.getUrl());
            rule.setMediaId(mediaId);
            rule.setMediaUrl(media.getUrl());
            rule.setMediaExpiresAt(LocalDateTime.now().plusDays(MEDIA_EXPIRE_DAYS));
        } catch (Exception e) {
            log.warn("[welcome] 临时素材上传失败，将在发送时重传。corpId={}, url={}",
                    rule.getCorpId(), media.getUrl(), e);
            rule.setMediaId(null);
            rule.setMediaUrl(media.getUrl());
            rule.setMediaExpiresAt(null);
        }
    }

    private String deriveFilename(String url) {
        String path = url.split("[?#]")[0];
        int idx = path.lastIndexOf('/');
        String name = idx >= 0 ? path.substring(idx + 1) : path;
        return StringUtils.hasText(name) ? name : "media";
    }

    /**
     * 绑定整组替换（欢迎语即分时规则，同一成员/部门可同时绑定多条
     * 时间不重叠的欢迎语）：目标在其他欢迎语上的绑定与当前欢迎语
     * 分时规则时间重叠时，默认阻断并提示归属，forceBind 时移除那些
     * 重叠绑定后改绑到当前欢迎语。
     */
    private void saveBinds(WxWelcomeMsgSaveReq req, Long welcomeMsgId, boolean forceBind) {
        String corpId = req.getCorpId();
        List<WelcomeTimeItem> currentRules =
                req.getTimeItems() == null ? List.of() : req.getTimeItems();
        List<WelcomeBindItem> newBinds = req.getBinds() == null ? List.of() : req.getBinds();

        // 入参去重
        Set<String> reqKeys = new HashSet<>();
        for (WelcomeBindItem bind : newBinds) {
            String key = bindKey(bind.getTargetType(), bind.getTargetId());
            if (!reqKeys.add(key)) {
                throw new CheckedException("绑定对象重复");
            }
        }

        List<WxWelcomeMsgBind> existing = wxWelcomeMsgBindMapper.selectList(
                Wrappers.<WxWelcomeMsgBind>lambdaQuery()
                        .eq(WxWelcomeMsgBind::getWelcomeMsgId, welcomeMsgId));

        // 跨欢迎语绑定冲突检测：目标在其他欢迎语上的绑定与当前欢迎语
        // 分时规则时间重叠时阻断，并提示归属（时间错开则可共存）
        if (!forceBind) {
            List<String> conflicts = checkBindConflicts(req);
            if (!conflicts.isEmpty()) {
                throw new CheckedException(BIND_CONFLICT_PREFIX
                        + String.join("；", conflicts)
                        + "。确认后时间重叠的绑定将改绑到当前欢迎语。");
            }
        }

        // 强制保存：移除目标在其他欢迎语上时间重叠的绑定（时间错开的保留）
        if (forceBind) {
            for (WelcomeBindItem bind : newBinds) {
                List<Long> overlappingIds = otherBindsOfTarget(corpId, bind, welcomeMsgId)
                        .stream()
                        .filter(row -> rulesOverlap(currentRules,
                                rulesOf(row.getWelcomeMsgId())))
                        .map(WxWelcomeMsgBind::getId)
                        .collect(Collectors.toList());
                if (!overlappingIds.isEmpty()) {
                    wxWelcomeMsgBindMapper.deleteBatchIds(overlappingIds);
                }
            }
        }

        // upsert 应用本次绑定（新插入；唯一键含 welcome_msg_id，重复保存幂等）
        for (WelcomeBindItem bind : newBinds) {
            WxWelcomeMsgBind row = new WxWelcomeMsgBind();
            row.setId(IdWorker.getId());
            row.setCorpId(corpId);
            row.setTargetType(bind.getTargetType());
            row.setTargetId(bind.getTargetId());
            row.setWelcomeMsgId(welcomeMsgId);
            row.setTenantId(TenantContextHolder.getTenantId());
            wxWelcomeMsgBindMapper.upsertBind(row);
        }

        // 解除不再属于本欢迎语的绑定
        List<Long> removedIds = existing.stream()
                .filter(row -> !reqKeys.contains(bindKey(row.getTargetType(), row.getTargetId())))
                .map(WxWelcomeMsgBind::getId)
                .collect(Collectors.toList());
        if (!removedIds.isEmpty()) {
            wxWelcomeMsgBindMapper.deleteBatchIds(removedIds);
        }
    }

    /** 查询目标在【其他欢迎语】上的绑定（currentWelcomeMsgId 为空时不排除） */
    private List<WxWelcomeMsgBind> otherBindsOfTarget(String corpId,
            WelcomeBindItem bind, Long currentWelcomeMsgId) {
        return wxWelcomeMsgBindMapper.selectList(
                Wrappers.<WxWelcomeMsgBind>lambdaQuery()
                        .eq(WxWelcomeMsgBind::getCorpId, corpId)
                        .eq(WxWelcomeMsgBind::getTargetType, bind.getTargetType())
                        .eq(WxWelcomeMsgBind::getTargetId, bind.getTargetId())
                        .ne(currentWelcomeMsgId != null,
                                WxWelcomeMsgBind::getWelcomeMsgId, currentWelcomeMsgId));
    }

    /** 加载某欢迎语的分时规则（按 sort、开始时间排序） */
    private List<WelcomeTimeItem> rulesOf(Long welcomeMsgId) {
        return wxWelcomeMsgRuleMapper.selectList(
                        Wrappers.<WxWelcomeMsgRule>lambdaQuery()
                                .eq(WxWelcomeMsgRule::getWelcomeMsgId, welcomeMsgId)
                                .orderByAsc(WxWelcomeMsgRule::getSort)
                                .orderByAsc(WxWelcomeMsgRule::getStartTime))
                .stream()
                .map(this::toTimeItem)
                .collect(Collectors.toList());
    }

    /** 两组分时规则在时间上是否重叠：任一规则对共享星期且时间段相交即重叠
     * （全天生效规则覆盖全部时间，与任何规则都重叠） */
    private boolean rulesOverlap(List<WelcomeTimeItem> currentRules,
            List<WelcomeTimeItem> otherRules) {
        for (WelcomeTimeItem current : currentRules) {
            for (WelcomeTimeItem other : otherRules) {
                if (isAllDayRule(current) || isAllDayRule(other)) {
                    return true;
                }
                boolean dayHit = current.getEffectiveAt().stream()
                        .anyMatch(other.getEffectiveAt()::contains);
                boolean timeHit =
                        current.getStartTime().isBefore(other.getEndTime())
                                && other.getStartTime().isBefore(current.getEndTime());
                if (dayHit && timeHit) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 绑定冲突描述：成员/部门「名称」已绑定欢迎语「标题」 */
    private String describeBindConflict(WxWelcomeMsgBind row) {
        String targetName;
        if (row.getTargetType() == BIND_TARGET_USER) {
            WxUser user = wxUserMapper.selectById(row.getTargetId());
            targetName = user != null && StringUtils.hasText(user.getName())
                    ? user.getName()
                    : String.valueOf(row.getTargetId());
            targetName = "成员「" + targetName + "」";
        } else {
            WxDepartment dept = wxDepartmentMapper.selectById(row.getTargetId());
            targetName = dept != null && StringUtils.hasText(dept.getName())
                    ? dept.getName()
                    : String.valueOf(row.getTargetId());
            targetName = "部门「" + targetName + "」";
        }
        String msgName = Optional.ofNullable(wxWelcomeMsgMapper.selectById(row.getWelcomeMsgId()))
                .map(WxWelcomeMsg::getName)
                .orElse(String.valueOf(row.getWelcomeMsgId()));
        return targetName + "已绑定欢迎语「" + msgName + "」";
    }

    private String bindKey(Integer targetType, Long targetId) {
        return targetType + ":" + targetId;
    }

    private List<WelcomeBindItem> listBinds(String corpId, Long welcomeMsgId) {
        List<WxWelcomeMsgBind> rows = wxWelcomeMsgBindMapper.selectList(
                Wrappers.<WxWelcomeMsgBind>lambdaQuery()
                        .eq(WxWelcomeMsgBind::getWelcomeMsgId, welcomeMsgId)
                        .orderByAsc(WxWelcomeMsgBind::getTargetType, WxWelcomeMsgBind::getTargetId));
        if (rows.isEmpty()) {
            return List.of();
        }

        // 分类型批量回填名称
        List<Long> userIds = rows.stream()
                .filter(row -> row.getTargetType() == BIND_TARGET_USER)
                .map(WxWelcomeMsgBind::getTargetId)
                .collect(Collectors.toList());
        List<Long> deptIds = rows.stream()
                .filter(row -> row.getTargetType() == BIND_TARGET_DEPT)
                .map(WxWelcomeMsgBind::getTargetId)
                .collect(Collectors.toList());

        Map<Long, String> userNames = userIds.isEmpty()
                ? Map.of()
                : wxUserMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(WxUser::getId,
                                user -> user.getName() != null ? user.getName() : String.valueOf(user.getId())));
        Map<Long, String> deptNames = deptIds.isEmpty()
                ? Map.of()
                : wxDepartmentMapper.selectBatchIds(deptIds).stream()
                        .collect(Collectors.toMap(WxDepartment::getId,
                                dept -> dept.getName() != null ? dept.getName() : String.valueOf(dept.getId())));

        return rows.stream()
                .map(row -> {
                    WelcomeBindItem item = new WelcomeBindItem();
                    item.setTargetType(row.getTargetType());
                    item.setTargetId(row.getTargetId());
                    if (row.getTargetType() == BIND_TARGET_USER) {
                        item.setTargetName(userNames.get(row.getTargetId()));
                    } else if (row.getTargetType() == BIND_TARGET_DEPT) {
                        item.setTargetName(deptNames.get(row.getTargetId()));
                    }
                    return item;
                })
                .collect(Collectors.toList());
    }

    private WelcomeTimeItem toTimeItem(WxWelcomeMsgRule rule) {
        WelcomeTimeItem item = new WelcomeTimeItem();
        item.setId(rule.getId());
        item.setName(rule.getName());
        item.setContent(parseContent(rule.getContent()));
        item.setEffectiveAt(rule.getEffectiveAt());
        item.setStartTime(rule.getStartTime());
        item.setEndTime(rule.getEndTime());
        return item;
    }

    private List<WelcomeContentItem> parseContent(String content) {
        if (!StringUtils.hasText(content)) {
            return List.of();
        }
        try {
            return GSON.fromJson(content, CONTENT_LIST_TYPE);
        } catch (Exception e) {
            log.warn("[welcome] content JSON 解析失败：{}", content, e);
            return List.of();
        }
    }

    // ==================== 添加客户事件触发发送 ====================

    @Override
    public void sendForAddContact(String corpId, String userId, String welcomeCode) {
        try {
            doSendForAddContact(corpId, userId, welcomeCode);
        } catch (WxErrorException e) {
            int errcode = e.getError() == null ? 0 : e.getError().getErrorCode();
            if (errcode == 41051) {
                // 欢迎语已成功下发后再次调用：无需重试
                log.info("[welcome] 欢迎语已下发过（41051），跳过：corpId={}, userId={}", corpId, userId);
            } else if (errcode == 41096) {
                // 其他应用正在发送欢迎语：不代表成功，但 20 秒窗口内无法重试
                log.warn("[welcome] 其他应用正在发送欢迎语（41096），本次放弃：corpId={}, userId={}", corpId, userId);
            } else {
                log.warn("[welcome] 添加客户触发欢迎语发送失败：corpId={}, userId={}",
                        corpId, userId, e);
            }
        } catch (Exception e) {
            log.warn("[welcome] 添加客户触发欢迎语发送失败：corpId={}, userId={}",
                    corpId, userId, e);
        }
    }

    private void doSendForAddContact(String corpId, String userId, String welcomeCode)
            throws WxErrorException {
        if (!StringUtils.hasText(corpId) || !StringUtils.hasText(userId)
                || !StringUtils.hasText(welcomeCode)) {
            return;
        }
        WxUser user = wxUserMapper.selectOne(Wrappers.<WxUser>lambdaQuery()
                .eq(WxUser::getCorpId, corpId)
                .eq(WxUser::getUserId, userId));
        if (user == null) {
            log.info("[welcome] 未找到跟进成员，跳过欢迎语：corpId={}, userId={}", corpId, userId);
            return;
        }
        // 命中「成员本身」或「其所属部门」的绑定
        List<WxWelcomeMsgBind> binds = wxWelcomeMsgBindMapper.selectList(
                Wrappers.<WxWelcomeMsgBind>lambdaQuery()
                        .eq(WxWelcomeMsgBind::getCorpId, corpId)
                        .and(w -> {
                            w.eq(WxWelcomeMsgBind::getTargetType, BIND_TARGET_USER)
                                    .eq(WxWelcomeMsgBind::getTargetId, user.getId());
                            if (!CollectionUtils.isEmpty(user.getDepartIds())) {
                                w.or(w2 -> w2.eq(WxWelcomeMsgBind::getTargetType, BIND_TARGET_DEPT)
                                        .in(WxWelcomeMsgBind::getTargetId, user.getDepartIds()));
                            }
                        }));
        if (binds.isEmpty()) {
            log.info("[welcome] 成员/部门未绑定欢迎语，跳过：corpId={}, userId={}", corpId, userId);
            return;
        }
        // 绑定的欢迎语规则按 sort（匹配顺序）排列，命中当前星期/时间段的第一条即发
        List<Long> msgIds = binds.stream()
                .map(WxWelcomeMsgBind::getWelcomeMsgId)
                .distinct()
                .collect(Collectors.toList());
        List<WxWelcomeMsgRule> rules = wxWelcomeMsgRuleMapper.selectList(
                Wrappers.<WxWelcomeMsgRule>lambdaQuery()
                        .in(WxWelcomeMsgRule::getWelcomeMsgId, msgIds)
                        .orderByAsc(WxWelcomeMsgRule::getSort));
        WxWelcomeMsgRule matched = rules.stream()
                .filter(this::ruleMatchesNow)
                .findFirst()
                .orElse(null);
        if (matched == null) {
            log.info("[welcome] 无命中时间段的规则，跳过：corpId={}, userId={}", corpId, userId);
            return;
        }
        WxCpWelcomeMsg welcomeMsg = buildWelcomeMsg(matched, welcomeCode);
        if (welcomeMsg == null) {
            log.warn("[welcome] 命中规则无可发送内容，跳过：ruleId={}", matched.getId());
            return;
        }
        wxCorpApiStrategy.getWxCpService(corpId)
                .getExternalContactService()
                .sendWelcomeMsg(welcomeMsg);
        log.info("[welcome] 添加客户触发欢迎语发送成功：corpId={}, userId={}, ruleId={}",
                corpId, userId, matched.getId());
    }

    /** 规则是否在当前星期与时间段生效（兼容跨天窗口，如 22:00~06:00） */
    private boolean ruleMatchesNow(WxWelcomeMsgRule rule) {
        List<Integer> days = rule.getEffectiveAt();
        if (CollectionUtils.isEmpty(days)
                || !days.contains(LocalDate.now().getDayOfWeek().getValue())) {
            return false;
        }
        LocalTime now = LocalTime.now();
        LocalTime start = rule.getStartTime();
        LocalTime end = rule.getEndTime();
        if (start == null || end == null) {
            return true;
        }
        if (end.isBefore(start)) {
            return !now.isBefore(start) || !now.isAfter(end);
        }
        return !now.isBefore(start) && !now.isAfter(end);
    }

    /** 附件数量上限（企微 send_welcome_msg：attachments 最多 9 项） */
    private static final int MAX_ATTACHMENTS = 9;

    /** 组装企微欢迎语：文本取首个文本项（超 4000 字节防御截断），
     * 附件取全部图片/视频/链接/文件/小程序项（最多 9 个，首个媒体使用规则级 media_id 缓存） */
    private WxCpWelcomeMsg buildWelcomeMsg(WxWelcomeMsgRule rule, String welcomeCode) {
        List<WelcomeContentItem> content = parseContent(rule.getContent());
        if (content.isEmpty()) {
            return null;
        }
        WxCpWelcomeMsg msg = new WxCpWelcomeMsg();
        msg.setWelcomeCode(welcomeCode);
        content.stream()
                .filter(item -> MSG_TEXT.equals(item.getMsgType()))
                .map(WelcomeContentItem::getTextContent)
                .filter(StringUtils::hasText)
                .findFirst()
                .ifPresent(text -> msg.setText(new Text().setContent(
                        utf8Truncate(text, 4000))));
        List<Attachment> attachments = new ArrayList<>();
        boolean firstMedia = true;
        for (WelcomeContentItem item : content) {
            if (attachments.size() >= MAX_ATTACHMENTS) {
                log.warn("[welcome] 附件超过 {} 个，已截断：ruleId={}", MAX_ATTACHMENTS, rule.getId());
                break;
            }
            if (!MSG_IMAGE.equals(item.getMsgType())
                    && !MSG_VIDEO.equals(item.getMsgType())
                    && !MSG_LINK.equals(item.getMsgType())
                    && !MSG_FILE.equals(item.getMsgType())
                    && !MSG_MINIPROGRAM.equals(item.getMsgType())) {
                continue;
            }
            // 首个媒体项使用规则级 media_id 缓存，其余每次发送时现传临时素材
            toAttachment(rule, item, firstMedia).ifPresent(att -> {
                attachments.add(att);
            });
            firstMedia = false;
        }
        if (!attachments.isEmpty()) {
            msg.setAttachments(attachments);
        }
        return msg.getText() == null && CollectionUtils.isEmpty(msg.getAttachments())
                ? null
                : msg;
    }

    private Optional<Attachment> toAttachment(WxWelcomeMsgRule rule, WelcomeContentItem item, boolean useMediaCache) {
        switch (item.getMsgType()) {
            case MSG_LINK -> {
                Link link = new Link();
                link.setTitle(item.getLinkTitle());
                link.setDesc(item.getLinkDesc());
                link.setUrl(item.getLinkUrl());
                link.setPicUrl(item.getUrl());
                return Optional.of(attachment(MSG_LINK, link, null, null, null));
            }
            case MSG_IMAGE -> {
                String mediaId = useMediaCache
                        ? ensureMediaId(rule, item)
                        : uploadTmpMediaQuietly(rule, item);
                return StringUtils.hasText(mediaId)
                        ? Optional.of(attachment(MSG_IMAGE, null,
                                new Image().setMediaId(mediaId), null, null))
                        : Optional.empty();
            }
            case MSG_VIDEO -> {
                String mediaId = useMediaCache
                        ? ensureMediaId(rule, item)
                        : uploadTmpMediaQuietly(rule, item);
                return StringUtils.hasText(mediaId)
                        ? Optional.of(attachment(MSG_VIDEO, null, null,
                                new Video().setMediaId(mediaId), null))
                        : Optional.empty();
            }
            case MSG_FILE -> {
                String mediaId = useMediaCache
                        ? ensureMediaId(rule, item)
                        : uploadTmpMediaQuietly(rule, item);
                return StringUtils.hasText(mediaId)
                        ? Optional.of(attachment(MSG_FILE, null, null, null,
                                new File().setMediaId(mediaId)))
                        : Optional.empty();
            }
            case MSG_MINIPROGRAM -> {
                // 封面图须转临时素材取 pic_media_id（建议 520x416），每次现传
                MiniProgram miniProgram = new MiniProgram();
                miniProgram.setTitle(utf8Truncate(item.getMpTitle(), 64));
                miniProgram.setAppid(item.getMpAppid());
                miniProgram.setPage(item.getMpPage());
                try {
                    miniProgram.setPicMediaId(wxMediaService.uploadTmpMedia(
                            rule.getCorpId(), MSG_IMAGE, deriveFilename(item.getUrl()), item.getUrl()));
                } catch (Exception e) {
                    log.warn("[welcome] 小程序封面上传失败，跳过该附件：ruleId={}, url={}",
                            rule.getId(), item.getUrl(), e);
                    return Optional.empty();
                }
                return Optional.of(attachment(MSG_MINIPROGRAM, null, null, null, null))
                        .map(att -> {
                            att.setMiniProgram(miniProgram);
                            return att;
                        });
            }
            default -> {
                return Optional.empty();
            }
        }
    }

    /** Attachment.setMsgType 无链式返回，这里统一组装 */
    private Attachment attachment(String msgType, Link link, Image image, Video video, File file) {
        Attachment attachment = new Attachment();
        attachment.setMsgType(msgType);
        attachment.setLink(link);
        attachment.setImage(image);
        attachment.setVideo(video);
        attachment.setFile(file);
        return attachment;
    }

    /** 发送时确保 media_id 有效：缺失/过期/地址变更则按 media_url 重传并回写规则（失败返回 null，仅发文本） */
    private String ensureMediaId(WxWelcomeMsgRule rule, WelcomeContentItem item) {
        boolean needUpload = !StringUtils.hasText(rule.getMediaId())
                || !StringUtils.hasText(rule.getMediaUrl())
                || !item.getUrl().equals(rule.getMediaUrl())
                || rule.getMediaExpiresAt() == null
                || rule.getMediaExpiresAt().isBefore(LocalDateTime.now().plusHours(2));
        if (!needUpload) {
            return rule.getMediaId();
        }
        try {
            String filename = StringUtils.hasText(item.getFilename())
                    ? item.getFilename()
                    : deriveFilename(item.getUrl());
            String mediaId = wxMediaService.uploadTmpMedia(
                    rule.getCorpId(), item.getMsgType(), filename, item.getUrl());
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(MEDIA_EXPIRE_DAYS);
            rule.setMediaId(mediaId);
            rule.setMediaUrl(item.getUrl());
            rule.setMediaExpiresAt(expiresAt);
            WxWelcomeMsgRule update = new WxWelcomeMsgRule();
            update.setId(rule.getId());
            update.setMediaId(mediaId);
            update.setMediaUrl(item.getUrl());
            update.setMediaExpiresAt(expiresAt);
            wxWelcomeMsgRuleMapper.updateById(update);
            return mediaId;
        } catch (Exception e) {
            log.warn("[welcome] 发送时重传临时素材失败，跳过媒体附件：ruleId={}, url={}",
                    rule.getId(), item.getUrl(), e);
            return null;
        }
    }

    /** 非首个媒体项：不走规则级缓存，每次发送现传临时素材（规则仅缓存首个媒体的 media_id） */
    private String uploadTmpMediaQuietly(WxWelcomeMsgRule rule, WelcomeContentItem item) {
        try {
            String filename = StringUtils.hasText(item.getFilename())
                    ? item.getFilename()
                    : deriveFilename(item.getUrl());
            return wxMediaService.uploadTmpMedia(
                    rule.getCorpId(), item.getMsgType(), filename, item.getUrl());
        } catch (Exception e) {
            log.warn("[welcome] 临时素材上传失败，跳过该媒体附件：ruleId={}, url={}",
                    rule.getId(), item.getUrl(), e);
            return null;
        }
    }
}
