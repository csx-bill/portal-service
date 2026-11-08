package com.bill.portal.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bill.portal.common.core.client.ClientContextHolder;
import com.bill.portal.common.core.model.R;
import com.bill.portal.system.dto.SysDictCreateReq;
import com.bill.portal.system.dto.SysDictItemCreateReq;
import com.bill.portal.system.dto.SysDictItemUpdateReq;
import com.bill.portal.system.dto.SysDictUpdateReq;
import com.bill.portal.system.entity.SysDict;
import com.bill.portal.system.entity.SysDictItem;
import com.bill.portal.system.service.ISysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/dict")
@Tag(name = "系统字典", description = "系统字典接口")
@RequiredArgsConstructor
public class SysDictController {

    private final ISysDictService sysDictService;

    @GetMapping("/list")
    @Operation(summary = "全量字典类型列表", description = "按 client_id 过滤，供左侧类型列表使用")
    public R<List<SysDict>> list() {
        String clientId = ClientContextHolder.getClientId();
        return R.success(sysDictService.listAll(clientId));
    }

    @GetMapping("/exists/type")
    @Operation(summary = "检查字典类型是否已存在")
    public R<Boolean> isTypeExists(@RequestParam("type") @Size(max = 100, message = "{dict.type.size}") String type,
                                   @RequestParam(value = "excludeId", required = false) Long excludeId) {
        return R.success(sysDictService.isTypeExists(type, excludeId, ClientContextHolder.getClientId()));
    }

    @PostMapping
    @SaCheckPermission("#clientId:dict:add")
    @Operation(summary = "新增", description = "新增字典")
    public R<Boolean> save(@RequestBody @Valid SysDictCreateReq params) {
        String clientId = ClientContextHolder.getClientId();
        SysDict sysDict = BeanUtil.copyProperties(params, SysDict.class);
        sysDict.setClientId(clientId);
        return R.success(sysDictService.save(sysDict));
    }

    @PutMapping
    @SaCheckPermission("#clientId:dict:update")
    @Operation(summary = "更新", description = "更新字典")
    public R<Boolean> updateById(@RequestBody @Valid SysDictUpdateReq params) {
        SysDict sysDict = BeanUtil.copyProperties(params, SysDict.class);
        return R.success(sysDictService.updateById(sysDict));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("#clientId:dict:delete")
    @Operation(summary = "删除", description = "删除字典（含其全部字典项；系统内置字典拒绝删除）")
    public R<Boolean> removeById(@PathVariable("id") Long id) {
        return R.success(sysDictService.deleteWithItems(id));
    }


    // ==================== 字典项 ====================

    /**
     * 通过字典类型查找字典
     * @param type 类型
     * @return 同类型字典
     */
    @GetMapping("/type/{type}")
    public R<List<SysDictItem>> getDictByType(@PathVariable("type") String type) {
        String clientId = ClientContextHolder.getClientId();
        return R.success(sysDictService.getDictByType(type, clientId));
    }

    @GetMapping("/item/list")
    @Operation(summary = "字典项列表", description = "指定字典下的全部字典项，按 sortOrder 排序")
    public R<List<SysDictItem>> listItems(@RequestParam("dictId") Long dictId) {
        return R.success(sysDictService.listItems(dictId));
    }

    @GetMapping("/item/exists/value")
    @Operation(summary = "检查字典项值是否已存在（同一字典内）")
    public R<Boolean> isItemValueExists(@RequestParam("dictId") Long dictId,
                                        @RequestParam("value") @Size(max = 100, message = "{dictItem.value.size}") String value,
                                        @RequestParam(value = "excludeId", required = false) Long excludeId) {
        return R.success(sysDictService.isItemValueExists(dictId, value, excludeId));
    }

    @PostMapping("/item")
    @SaCheckPermission("#clientId:dict:add")
    @Operation(summary = "新增字典项")
    public R<Boolean> saveItem(@RequestBody @Valid SysDictItemCreateReq params) {
        SysDictItem item = BeanUtil.copyProperties(params, SysDictItem.class);
        return R.success(sysDictService.saveItem(item));
    }

    @PutMapping("/item")
    @SaCheckPermission("#clientId:dict:update")
    @Operation(summary = "更新字典项")
    public R<Boolean> updateItem(@RequestBody @Valid SysDictItemUpdateReq params) {
        SysDictItem item = BeanUtil.copyProperties(params, SysDictItem.class);
        return R.success(sysDictService.updateItem(item));
    }

    @DeleteMapping("/item/{id}")
    @SaCheckPermission("#clientId:dict:delete")
    @Operation(summary = "删除字典项")
    public R<Boolean> deleteItem(@PathVariable("id") Long id) {
        return R.success(sysDictService.deleteItem(id));
    }
}
