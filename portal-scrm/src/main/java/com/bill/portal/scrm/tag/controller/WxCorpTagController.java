package com.bill.portal.scrm.tag.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bill.portal.common.core.model.R;
import com.bill.portal.common.core.tenant.TenantContextHolder;
import com.bill.portal.scrm.common.enums.SyncType;
import com.bill.portal.scrm.common.enums.TriggerType;
import com.bill.portal.scrm.tag.dto.WxCorpTagGroupQuery;
import com.bill.portal.scrm.tag.dto.WxTagGroupCreateReq;
import com.bill.portal.scrm.tag.dto.WxTagGroupUpdateReq;
import com.bill.portal.scrm.tag.entity.WxCorpTag;
import com.bill.portal.scrm.tag.entity.WxCorpTagGroup;
import com.bill.portal.scrm.tag.service.IWxCorpTagGroupService;
import com.bill.portal.scrm.tag.service.IWxCorpTagService;
import com.bill.portal.scrm.tag.service.IWxCorpTagSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scrm/tag")
@Tag(name = "企微标签管理")
@RequiredArgsConstructor
public class WxCorpTagController {

    private final IWxCorpTagService wxCorpTagService;
    private final IWxCorpTagGroupService wxCorpTagGroupService;
    private final IWxCorpTagSyncService wxCorpTagSyncService;


    @GetMapping("/groupList")
    @Operation(summary = "标签组列表")
    public R<List<WxCorpTagGroup>> groupList(@ParameterObject WxCorpTagGroupQuery query) {
        return R.success(wxCorpTagGroupService.groupList(query));
    }

    @GetMapping("/group/{id}")
    @Operation(summary = "通过标签组id查询")
    public R<WxCorpTagGroup> getGroupById(@PathVariable("id") Long id) {
        return R.success(wxCorpTagGroupService.getById(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "通过标签id查询")
    public R<WxCorpTag> getTagById(@PathVariable("id") Long id) {
        return R.success(wxCorpTagService.getById(id));
    }


    @PostMapping
    @SaCheckPermission("tag:add")
    @Operation(summary = "新增")
    public R<String> createTag(@RequestBody WxTagGroupCreateReq req) throws WxErrorException {
        wxCorpTagSyncService.createTag(req);
        return R.success();
    }

    @PutMapping
    @SaCheckPermission("tag:update")
    @Operation(summary = "更新")
    public R<Boolean> updateTag(@RequestBody WxTagGroupUpdateReq req) throws WxErrorException {
        wxCorpTagSyncService.updateTag(req);
        return R.success(true);
    }

    @DeleteMapping("/group/{id}")
    @SaCheckPermission("tag:delete")
    @Operation(summary = "通过标签组id删除", description = "通过标签组id删除")
    public R<Boolean> delGroup(@PathVariable("id") Long id) throws WxErrorException {
        wxCorpTagSyncService.delGroup(id);
        return R.success();
    }


    @PostMapping("/sync/trigger")
    @SaCheckPermission("tag:sync")
    @Operation(summary = "手动触发企业标签全量同步")
    public R<?> triggerSync(@RequestParam("corpId") String corpId) {
        Long tenantId = TenantContextHolder.getTenantId();
        wxCorpTagSyncService.executeFullSync(tenantId, corpId, SyncType.TAG_FULL.getCode(), TriggerType.MANUAL.getCode());
        return R.success();
    }
}
