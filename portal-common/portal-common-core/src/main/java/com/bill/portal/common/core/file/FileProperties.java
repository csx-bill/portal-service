package com.bill.portal.common.core.file;

import com.bill.portal.common.core.file.local.LocalFileProperties;
import com.bill.portal.common.core.file.s3.S3FileProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * 文件 配置信息
 * bucket 设置公共读权限
 */
@Data
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    /**
     * 默认的存储桶名称
     */
    private String bucketName = "local";

    /**
     * 文件上传类型，决定使用哪种存储实现
     */
    private UploadType uploadType = UploadType.LOCAL;

    /**
     * 本地文件配置信息
     */
    @NestedConfigurationProperty
    private LocalFileProperties local = new LocalFileProperties();

    /**
     * S3 文件配置信息
     */
    @NestedConfigurationProperty
    private S3FileProperties s3 = new S3FileProperties();

}
