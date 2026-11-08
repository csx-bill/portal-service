package com.bill.portal.scrm.corp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bill.portal.scrm.corp.entity.WxCorpAuth;

import java.util.List;

public interface IWxCorpAuthService extends IService<WxCorpAuth> {

    List<WxCorpAuth> getAuthorizedCorps();

    WxCorpAuth getCorpAuth(String corpId);

}
