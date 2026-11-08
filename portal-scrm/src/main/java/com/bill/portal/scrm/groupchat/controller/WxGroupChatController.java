package com.bill.portal.scrm.groupchat.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.common.core.model.R;
import com.bill.portal.common.core.tenant.TenantContextHolder;
import com.bill.portal.scrm.common.enums.SyncType;
import com.bill.portal.scrm.common.enums.TriggerType;
import com.bill.portal.scrm.groupchat.dto.GroupChatStatisticDTO;
import com.bill.portal.scrm.groupchat.dto.GroupMemberStatisticDTO;
import com.bill.portal.scrm.groupchat.dto.WxGroupChatQuery;
import com.bill.portal.scrm.groupchat.entity.WxGroupChat;
import com.bill.portal.scrm.groupchat.entity.WxGroupChatMember;
import com.bill.portal.scrm.groupchat.service.IWxGroupChatService;
import com.bill.portal.scrm.groupchat.service.IWxGroupChatSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 企微客户群管理
 */
@RestController
@RequestMapping("/scrm/groupchat")
@Tag(name = "企微客户群管理")
@AllArgsConstructor
public class WxGroupChatController {

    private final IWxGroupChatSyncService wxGroupChatSyncService;
    private final IWxGroupChatService wxGroupChatService;

    @PostMapping("/sync/trigger")
    @SaCheckPermission("groupchat:sync")
    @Operation(summary = "手动触发客户群全量同步")
    public R<?> triggerSync(@RequestParam("corpId") String corpId) {
        TenantContextHolder.setTenantId(1L);
        wxGroupChatSyncService.executeFullSync(1L,corpId, SyncType.GROUP_CHAT_FULL.getCode(), TriggerType.MANUAL.getCode());
        return R.success();
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public R<IPage<WxGroupChat>> page(@ParameterObject Page<WxGroupChat> page, @ParameterObject @Valid WxGroupChatQuery query) {
        return R.success(wxGroupChatService.page(page, query));
    }

    @GetMapping("/list")
    @Operation(summary = "列表查询")
    public R<List<WxGroupChat>> list(@ParameterObject @Valid WxGroupChatQuery query) {
        return R.success(wxGroupChatService.list(query));
    }

    @GetMapping("/member/list")
    @Operation(summary = "群成员列表", description = "按入群时间升序返回指定客户群的成员")
    public R<List<WxGroupChatMember>> memberList(@RequestParam("chatId") String chatId) {
        return R.success(wxGroupChatService.listMembers(chatId));
    }

    @GetMapping("/member/statistic")
    @Operation(summary = "群成员变动统计", description = "统计指定日期的入群/退群数量，date 不传默认当天")
    public R<GroupMemberStatisticDTO> memberStatistic(
            @RequestParam("chatId") String chatId,
            @RequestParam(value = "corpId", required = false) String corpId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return R.success(wxGroupChatService.memberStatistic(corpId, chatId, date));
    }

    @GetMapping("/member/statistic/list")
    @Operation(summary = "按企业聚合的各群成员变动统计", description = "统计指定日期各客户群的入群/退群数量，date 不传默认当天")
    public R<List<GroupMemberStatisticDTO>> memberStatisticByCorp(
            @RequestParam("corpId") String corpId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return R.success(wxGroupChatService.memberStatisticByCorp(corpId, date));
    }

    @GetMapping("/statistic/summary")
    @Operation(summary = "客户群统计-当日汇总", description = "实时口径：本地表即时计算（新增群/新增人数/群总数/群总人数）")
    public R<GroupChatStatisticDTO> statisticSummary(
            @RequestParam("corpId") String corpId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return R.success(wxGroupChatService.statisticSummary(corpId, date));
    }

}
