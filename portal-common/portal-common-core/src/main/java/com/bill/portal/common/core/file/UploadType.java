package com.bill.portal.common.core.file;

/**
 * 文件上传类型，通过 file.upload-type 配置
 */
public enum UploadType {

    /**
     * 本地存储
     */
    LOCAL,

    /**
     * S3 协议对象存储，兼容 MinIO、阿里云 OSS 等
     */
    S3

}
