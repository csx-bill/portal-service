package com.bill.portal.scrm.acquisition.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bill.portal.scrm.acquisition.entity.WxAcquisitionLink;
import com.bill.portal.scrm.acquisition.mapper.WxAcquisitionLinkMapper;
import com.bill.portal.scrm.acquisition.service.IWxAcquisitionLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WxAcquisitionLinkServiceImpl extends ServiceImpl<WxAcquisitionLinkMapper, WxAcquisitionLink> implements IWxAcquisitionLinkService {

}
