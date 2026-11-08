package com.bill.portal.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.common.core.client.ClientContextHolder;
import com.bill.portal.common.core.model.R;
import com.bill.portal.system.dto.SysFileBatchReq;
import com.bill.portal.system.dto.SysFileCreateReq;
import com.bill.portal.system.dto.SysFileQuery;
import com.bill.portal.system.dto.SysFileUpdateReq;
import com.bill.portal.system.dto.SysFileUploadDTO;
import com.bill.portal.system.entity.SysFile;
import com.bill.portal.system.service.ISysFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/system/file")
@Tag(name = "系统文件", description = "系统文件接口（元数据 + 本地存储）")
@RequiredArgsConstructor
public class SysFileController {

    private final ISysFileService sysFileService;

    @GetMapping("/page")
    @Operation(summary = "分页查询", description = "分页查询（支持分组/未分组/类型分类/文件名过滤）")
    public R<IPage<SysFile>> page(@ParameterObject Page<SysFile> page,
                                  @ParameterObject @Valid SysFileQuery query) {
        return R.success(sysFileService.pageFiles(page, query));
    }

    @PostMapping("/upload")
    @SaCheckPermission("#clientId:file:upload")
    @Operation(summary = "上传文件", description = "上传文件（落盘 + 写入元数据），可选指定分组；返回 fileName 与访问地址 url")
    public R<SysFileUploadDTO> upload(@RequestPart("file") MultipartFile file, @RequestParam(value = "dir", required = false) String dir,
                             @RequestParam(value = "groupId", required = false) Long groupId,
                             @RequestParam(value = "type", required = false) String type) throws Exception {
        return R.success(sysFileService.uploadFile(file, dir, groupId, type));
    }


    @PostMapping
    @SaCheckPermission("#clientId:file:add")
    @Operation(summary = "新增", description = "新增文件")
    public R<Boolean> save(@RequestBody @Valid SysFileCreateReq params) {
        String clientId = ClientContextHolder.getClientId();
        SysFile sysFile = BeanUtil.copyProperties(params, SysFile.class);
        sysFile.setClientId(clientId);
        return R.success(sysFileService.save(sysFile));
    }

    @PutMapping
    @SaCheckPermission("#clientId:file:update")
    @Operation(summary = "更新", description = "更新文件（重命名 / 调整分组）")
    public R<Boolean> updateById(@RequestBody @Valid SysFileUpdateReq params) {
        SysFile sysFile = BeanUtil.copyProperties(params, SysFile.class);
        return R.success(sysFileService.updateById(sysFile));
    }

    @PostMapping("/move")
    @SaCheckPermission("#clientId:file:update")
    @Operation(summary = "批量移动", description = "批量移动文件到指定分组（groupId 为空表示移到未分组）")
    public R<Boolean> move(@RequestBody @Valid SysFileBatchReq params) {
        String clientId = ClientContextHolder.getClientId();
        return R.success(sysFileService.moveBatch(params.getIds(), params.getGroupId(), clientId));
    }

    @DeleteMapping
    @SaCheckPermission("#clientId:file:delete")
    @Operation(summary = "批量删除", description = "批量删除文件（含物理文件）")
    public R<Boolean> removeBatch(@RequestBody @Valid SysFileBatchReq params) {
        String clientId = ClientContextHolder.getClientId();
        return R.success(sysFileService.deleteBatch(params.getIds(), clientId));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("#clientId:file:delete")
    @Operation(summary = "删除", description = "删除文件（含物理文件）")
    public R<Boolean> removeById(@PathVariable("id") Long id) {
        String clientId = ClientContextHolder.getClientId();
        return R.success(sysFileService.deleteBatch(java.util.List.of(id), clientId));
    }
}
