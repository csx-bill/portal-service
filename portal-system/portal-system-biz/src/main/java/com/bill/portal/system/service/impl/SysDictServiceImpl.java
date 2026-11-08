package com.bill.portal.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bill.portal.common.core.exception.CheckedException;
import com.bill.portal.system.entity.SysDict;
import com.bill.portal.system.entity.SysDictItem;
import com.bill.portal.system.mapper.SysDictItemMapper;
import com.bill.portal.system.mapper.SysDictMapper;
import com.bill.portal.system.service.ISysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements ISysDictService {

    private final SysDictItemMapper sysDictItemMapper;

    @Override
    public List<SysDict> listAll(String clientId) {
        return list(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getClientId, clientId)
                .orderByAsc(SysDict::getDictType));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteWithItems(Long id) {
        SysDict dict = getById(id);
        if (dict == null) {
            return true;
        }
        if ("1".equals(dict.getSystemFlag())) {
            throw new CheckedException("dict.system.cannotDelete");
        }
        sysDictItemMapper.delete(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictId, id));
        return removeById(id);
    }

    @Override
    public boolean isTypeExists(String dictType, Long excludeId, String clientId) {
        return lambdaQuery()
                .eq(SysDict::getDictType, dictType)
                .eq(SysDict::getClientId, clientId)
                .ne(excludeId != null, SysDict::getId, excludeId)
                .exists();
    }

    @Override
    public List<SysDictItem> getDictByType(String dictType, String clientId) {
        return sysDictItemMapper.selectList(Wrappers.<SysDictItem>query()
                .lambda()
                .eq(SysDictItem::getDictType, dictType)
                .eq(SysDictItem::getClientId, clientId)
                .orderByDesc(SysDictItem::getSortOrder));
    }

    @Override
    public List<SysDictItem> listItems(Long dictId) {
        return sysDictItemMapper.selectList(new LambdaQueryWrapper<SysDictItem>()
                        .eq(SysDictItem::getDictId, dictId))
                .stream()
                .sorted(Comparator.comparing(SysDictItem::getSortOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    @Override
    public boolean saveItem(SysDictItem item) {
        SysDict dict = getById(item.getDictId());
        if (dict != null) {
            item.setDictType(dict.getDictType());
            item.setClientId(dict.getClientId());
        }
        return sysDictItemMapper.insert(item) > 0;
    }

    @Override
    public boolean updateItem(SysDictItem item) {
        return sysDictItemMapper.updateById(item) > 0;
    }

    @Override
    public boolean deleteItem(Long id) {
        return sysDictItemMapper.deleteById(id) > 0;
    }

    @Override
    public boolean isItemValueExists(Long dictId, String itemValue, Long excludeId) {
        Long count = sysDictItemMapper.selectCount(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictId, dictId)
                .eq(SysDictItem::getItemValue, itemValue)
                .ne(excludeId != null, SysDictItem::getId, excludeId));
        return count != null && count > 0;
    }
}
