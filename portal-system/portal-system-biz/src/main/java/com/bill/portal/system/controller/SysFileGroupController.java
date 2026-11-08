package com.bill.portal.system.controller;

import cn.hutool.core.bean.BeanUtil;
import com.bill.portal.common.core.client.ClientContextHolder;
import com.bill.portal.common.core.exception.CheckedException;
import com.bill.portal.common.core.model.R;
import com.bill.portal.system.dto.SysFileGroupCreateReq;
import com.bill.portal.system.dto.SysFileGroupUpdateReq;
import com.bill.portal.system.entity.SysFileGroup;
import com.bill.portal.system.service.ISysFileGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/file/group")
@Tag(name = "系统文件分组", description = "系统文件分组接口")
@RequiredArgsConstructor
public class SysFileGroupController {

    private final ISysFileGroupService sysFileGroupService;

    @GetMapping("/tree")
    @Operation(summary = "分组树", description = "按 client_id 过滤的完整分组树")
    public R<List<SysFileGroup>> getGroupTree() {
        String clientId = ClientContextHolder.getClientId();
        return R.success(sysFileGroupService.getGroupTree(clientId));
    }

    @PostMapping
    @Operation(summary = "新增", description = "新增文件分组")
    public R<Boolean> save(@RequestBody @Valid SysFileGroupCreateReq params) {
        String clientId = ClientContextHolder.getClientId();
        SysFileGroup group = BeanUtil.copyProperties(params, SysFileGroup.class);
        group.setClientId(clientId);
        return R.success(sysFileGroupService.save(group));
    }

    @PutMapping
    @Operation(summary = "更新", description = "更新文件分组")
    public R<Boolean> updateById(@RequestBody @Valid SysFileGroupUpdateReq params) {
        SysFileGroup group = BeanUtil.copyProperties(params, SysFileGroup.class);
        return R.success(sysFileGroupService.updateById(group));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除", description = "删除文件分组（存在子分组或文件时拒绝删除）")
    public R<Boolean> removeById(@PathVariable("id") Long id) {
        if (sysFileGroupService.hasChildren(id)) {
            throw new CheckedException("fileGroup.hasChildren");
        }
        if (sysFileGroupService.hasFiles(id)) {
            throw new CheckedException("fileGroup.hasFiles");
        }
        return R.success(sysFileGroupService.removeById(id));
    }
}
