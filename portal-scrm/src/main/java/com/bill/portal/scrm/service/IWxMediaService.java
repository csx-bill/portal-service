package com.bill.portal.scrm.service;

import me.chanjar.weixin.common.error.WxErrorException;

import java.io.IOException;

public interface IWxMediaService {

    /**
     * 上传临时素材
     * @param corpId
     * @param mediaType
     * @param filename
     * @param url
     * @return
     */
    String uploadTmpMedia(String corpId,String mediaType, String filename, String url) throws IOException, WxErrorException;

    /**
     * 上传附件资源
     * @param corpId
     * @param mediaType 媒体文件类型，分别有图片（image）、视频（video）、普通文件（file）
     * @param attachmentType 附件类型，不同的附件类型用于不同的场景。1：朋友圈；2:商品图册
     * @param filename
     * @param url
     * @return
     */
    String uploadAttachment(String corpId,String mediaType,Integer attachmentType, String filename, String url) throws IOException, WxErrorException;

}
