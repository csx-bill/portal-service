package com.bill.portal.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bill.portal.system.entity.SysDict;
import com.bill.portal.system.entity.SysDictItem;

import java.util.List;

public interface ISysDictService extends IService<SysDict> {

    /** 全量字典类型列表（按 client_id 过滤，按 dictType 排序） */
    List<SysDict> listAll(String clientId);

    /** 删除字典（含其全部字典项；系统内置字典抛 CheckedException，message 为消息 key） */
    boolean deleteWithItems(Long id);

    /** 字典类型标识是否已存在 */
    boolean isTypeExists(String dictType, Long excludeId, String clientId);

    /** 通过字典类型查找字典 */
    List<SysDictItem> getDictByType(String dictType, String clientId);

    /** 指定字典下的全部字典项（按 sortOrder 排序） */
    List<SysDictItem> listItems(Long dictId);

    /** 新增字典项（冗余写入所属字典的 dictType / clientId） */
    boolean saveItem(SysDictItem item);

    boolean updateItem(SysDictItem item);

    boolean deleteItem(Long id);

    /** 同一字典下字典项值是否已存在 */
    boolean isItemValueExists(Long dictId, String itemValue, Long excludeId);
}
