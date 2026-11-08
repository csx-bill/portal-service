package com.bill.portal.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bill.portal.system.entity.SysFileGroup;

import java.util.List;

public interface ISysFileGroupService extends IService<SysFileGroup> {

    /** 分组树（按 client_id 过滤，parent_id 为空/0 视为根） */
    List<SysFileGroup> getGroupTree(String clientId);

    /** 分组下是否存在子分组 */
    boolean hasChildren(Long groupId);

    /** 分组下是否存在文件 */
    boolean hasFiles(Long groupId);
}
