package com.bill.portal.common.core.file;

import cn.hutool.core.util.StrUtil;
import com.bill.portal.common.core.file.local.LocalFileResourceConfig;
import com.bill.portal.common.core.file.local.LocalFileStorageServiceImpl;
import com.bill.portal.common.core.file.s3.S3FileProperties;
import com.bill.portal.common.core.file.s3.S3FileStorageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * 文件存储自动配置类
 * <p>
 * 由 file.upload-type 决定使用哪种存储：local（默认）或 s3，同一时刻只会有一个 FileStorageService
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(FileProperties.class)
@ConditionalOnClass(FileStorageService.class)
public class FileAutoConfiguration {


    /**
     * 本地文件存储服务
     */
    @Bean
    @ConditionalOnMissingBean(FileStorageService.class)
    @ConditionalOnProperty(name = "file.upload-type", havingValue = "local", matchIfMissing = true)
    public FileStorageService localFileStorageService(FileProperties properties) {
        log.info("文件上传类型: local, basePath={}", properties.getLocal().getBasePath());
        return new LocalFileStorageServiceImpl(properties);
    }

    /**
     * 本地文件直服：/{bucket}/** 映射到 {basePath}/{bucket}/，
     * 本地开发没有 nginx 时也能按接口下发的地址访问文件
     */
    @Bean
    @ConditionalOnProperty(name = "file.upload-type", havingValue = "local", matchIfMissing = true)
    public WebMvcConfigurer localFileResourceConfig(FileProperties properties) {
        return new LocalFileResourceConfig(properties);
    }

    /**
     * S3 客户端，已自定义 S3Client 时不再创建
     */
    @Bean
    @ConditionalOnMissingBean(S3Client.class)
    @ConditionalOnProperty(name = "file.upload-type", havingValue = "s3")
    public S3Client s3Client(FileProperties properties) {
        S3FileProperties s3 = properties.getS3();
        log.info("文件上传类型: s3, endpoint={}, region={}, pathStyleAccess={}",
                s3.getEndpoint(), s3.getRegion(), s3.isPathStyleAccess());
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(StrUtil.blankToDefault(s3.getRegion(), "us-east-1")))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(s3.isPathStyleAccess())
                        .chunkedEncodingEnabled(s3.isChunkedEncoding())
                        .build());
        if (StrUtil.isNotBlank(s3.getEndpoint())) {
            builder.endpointOverride(URI.create(s3.getEndpoint()));
        }
        if (StrUtil.isNotBlank(s3.getAccessKey()) && StrUtil.isNotBlank(s3.getSecretKey())) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey())));
        }
        return builder.build();
    }

    /**
     * S3 文件存储服务
     */
    @Bean
    @ConditionalOnMissingBean(FileStorageService.class)
    @ConditionalOnProperty(name = "file.upload-type", havingValue = "s3")
    public FileStorageService s3FileStorageService(S3Client s3Client, FileProperties properties) {
        return new S3FileStorageServiceImpl(s3Client, properties.getS3());
    }

}
