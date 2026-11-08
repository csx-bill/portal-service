package com.bill.portal.scrm.service.impl;

import com.bill.portal.scrm.corp.strategy.WxCorpApiStrategy;
import com.bill.portal.scrm.service.IWxMediaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.WxCpMediaService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * 企微素材服务
 */
@Slf4j
@Service
@AllArgsConstructor
public class WxMediaServiceImpl implements IWxMediaService {

    private final WxCorpApiStrategy wxCorpApiStrategy;


    @Override
    public String uploadTmpMedia(String corpId, String mediaType, String filename, String url) throws IOException, WxErrorException {
        WxCpMediaService mediaService = wxCorpApiStrategy.getWxCpService(corpId).getMediaService();
        WxMediaUploadResult upload = mediaService.upload(mediaType, filename, url);
        return upload.getMediaId();
    }

    @Override
    public String uploadAttachment(String corpId,String mediaType,Integer attachmentType, String filename, String url) throws IOException, WxErrorException {
        String fileType = url.substring(url.lastIndexOf(".") + 1);
        // 1. 将网络地址转换为 URL 对象
        URL fileUrl = new URL(url);

        // 2. 打开网络资源的输入流
        try (InputStream inputStream = fileUrl.openStream()) {
            // 3. 直接调用你现有的方法
            WxMediaUploadResult wxMediaUploadResult = wxCorpApiStrategy.getWxCpService(corpId).getExternalContactService().uploadAttachment(mediaType, fileType, attachmentType, inputStream);
            return wxMediaUploadResult.getMediaId();
        }
    }
}
