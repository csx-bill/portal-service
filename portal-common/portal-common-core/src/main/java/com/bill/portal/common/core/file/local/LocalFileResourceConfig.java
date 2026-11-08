package com.bill.portal.common.core.file.local;

import cn.hutool.core.util.StrUtil;
import com.bill.portal.common.core.file.FileProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * 本地文件直服：把 /{bucket}/** 映射到 {basePath}/{bucket}/ 目录
 * <p>
 * 地址规则与生产 nginx 的 location /{bucket}/ { alias ... } 一致，本地开发没有 nginx 时
 * 也能按接口下发的地址访问文件；生产环境该前缀由 nginx 直接截走，请求不会进到应用
 */
@Slf4j
@AllArgsConstructor
public class LocalFileResourceConfig implements WebMvcConfigurer {

    private final FileProperties properties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String bucketName = properties.getBucketName();
        // 桶名为空时映射会变成 /**，把整个存储目录挂到根路径上，这里直接跳过
        if (StrUtil.isBlank(bucketName)) {
            log.warn("文件桶名称为空，跳过本地文件直服映射");
            return;
        }
        // 与 LocalFileStorageServiceImpl 的落盘规则保持一致，且必须以 / 结尾，否则会被当成文件名前缀
        String location = StrUtil.addSuffixIfNot(Paths.get(properties.getLocal().getBasePath(), bucketName)
                .toAbsolutePath().normalize().toUri().toString(), "/");
        registry.addResourceHandler("/" + bucketName + "/**")
                .addResourceLocations(location);
        log.info("本地文件直服: /{}/** -> {}", bucketName, location);
    }
}
