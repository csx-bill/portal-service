package com.bill.portal.scrm.corp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bill.portal.scrm.core.constant.WxConstants;
import com.bill.portal.scrm.corp.entity.WxCorpAuth;
import com.bill.portal.scrm.corp.mapper.WxCorpAuthMapper;
import com.bill.portal.scrm.corp.service.IWxCorpAuthService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WxCorpAuthServiceImpl extends ServiceImpl<WxCorpAuthMapper, WxCorpAuth> implements IWxCorpAuthService {

    /**
     * 取所有授权正常的企业列表
     */
    @Override
    public List<WxCorpAuth> getAuthorizedCorps() {
        LambdaQueryWrapper<WxCorpAuth> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WxCorpAuth::getAuthStatus, 1)
                .eq(WxCorpAuth::getApp, WxConstants.XQT_SCRM);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public WxCorpAuth getCorpAuth(String corpId) {
        return getOne(Wrappers.<WxCorpAuth>lambdaQuery().eq(WxCorpAuth::getCorpId, corpId));
    }
}
