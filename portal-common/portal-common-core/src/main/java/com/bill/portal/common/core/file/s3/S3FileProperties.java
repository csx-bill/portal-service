package com.bill.portal.common.core.file.s3;

import lombok.Data;

/**
 * S3 文件 配置信息
 * 兼容 AWS S3、MinIO、阿里云 OSS 等 S3 协议对象存储
 */
@Data
public class S3FileProperties {

    /**
     * 服务地址，如 http://127.0.0.1:9000；为空时使用 AWS 默认地址
     */
    private String endpoint;

    /**
     * 生成预签名地址时使用的对外服务地址，客户端访问不到 endpoint（如容器内网地址）时配置
     */
    private String publicEndpoint;

    /**
     * 区域
     */
    private String region = "us-east-1";

    /**
     * 访问密钥；为空时使用默认凭证链（环境变量、IAM 角色等）
     */
    private String accessKey;

    /**
     * 访问密钥
     */
    private String secretKey;

    /**
     * 是否使用 path style 访问。
     * 默认 false（virtual-host 风格，桶名作为子域，AWS S3 及新 SDK 的默认做法）；
     * MinIO 等自建服务用 IP 直连、未配置 MINIO_DOMAIN 时需改为 true
     */
    private boolean pathStyleAccess = false;

    /**
     * 上传时是否使用 aws-chunked 请求体编码，SDK 默认为 true；
     * 阿里云 OSS 不支持 aws-chunked，接 OSS 时需设为 false
     */
    private boolean chunkedEncoding = true;

}
