package com.bill.portal.scrm.tag.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bill.portal.scrm.tag.dto.WxCorpTagGroupQuery;
import com.bill.portal.scrm.tag.entity.WxCorpTagGroup;

import java.util.List;

public interface IWxCorpTagGroupService extends IService<WxCorpTagGroup> {
    List<WxCorpTagGroup> groupList(WxCorpTagGroupQuery query);
}
