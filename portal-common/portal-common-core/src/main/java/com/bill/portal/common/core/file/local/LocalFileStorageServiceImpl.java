package com.bill.portal.common.core.file.local;

import cn.hutool.core.util.StrUtil;
import com.bill.portal.common.core.client.ClientContextHolder;
import com.bill.portal.common.core.file.FileProperties;
import com.bill.portal.common.core.file.FileStorageService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@AllArgsConstructor
public class LocalFileStorageServiceImpl implements FileStorageService {

    private final FileProperties properties;

    @SneakyThrows
    @Override
    public void createBucket(String bucketName) {
        Files.createDirectories(Paths.get(properties.getLocal().getBasePath(), bucketName));
    }

    @Override
    public void putObject(String bucketName, String dir, String fileName, InputStream stream, long contentLength,
                          String contextType) throws Exception {
        // 检查存储桶是否存在，如果不存在则创建
        if (!Files.isDirectory(Paths.get(properties.getLocal().getBasePath(), bucketName))) {
            createBucket(bucketName);
        }
        // 写入文件，本地写入不需要文件大小
        Path filePath = resolvePath(bucketName, dir, fileName);
        Files.createDirectories(filePath.getParent());

        Files.copy(stream, filePath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public String getAccessUrl(String bucketName, String dir, String fileName) {
        // 走一遍 resolvePath，越界等非法入参在这里就被挡住，同时保证地址与落盘位置一致
        Path target = resolvePath(bucketName, dir, fileName);
        Path bucketPath = Paths.get(properties.getLocal().getBasePath(), bucketName).normalize();
        String relativePath = StreamSupport.stream(bucketPath.relativize(target).spliterator(), false)
                .map(Path::toString)
                .collect(Collectors.joining("/"));
        // 桶名即访问地址的一级路径，由 nginx 映射到 basePath 下的同名子目录（/local/ → {basePath}/local/）
        return "/" + bucketName + "/" + relativePath;
    }


    @Override
    public void deleteObject(String bucketName, String dir, String fileName) {
        Path filePath = resolvePath(bucketName, dir, fileName);
        try {
            // 只删文件不删目录，文件不存在时视为已删除
            if (Files.isRegularFile(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("删除文件失败: " + filePath, e);
        }
    }

    @Override
    public void deleteObjects(String bucketName, String dir, Collection<String> fileNames) {
        for (String fileName : fileNames) {
            deleteObject(bucketName, dir, fileName);
        }
    }

    /**
     * 解析文件路径，规则与 {@link #putObject} 保持一致：
     * basePath/bucketName/clientId/dir/fileName，clientId 与 dir 为空时跳过对应层级
     */
    private Path resolvePath(String bucketName, String dir, String fileName) {
        Path bucketPath = Paths.get(properties.getLocal().getBasePath(), bucketName).normalize();
        // 按应用隔离
        Path rootPath = bucketPath;
        String clientId = ClientContextHolder.getClientId();
        if (StrUtil.isNotBlank(clientId)) {
            rootPath = rootPath.resolve(clientId).normalize();
        }
        Path filePath = rootPath;
        if (StrUtil.isNotBlank(dir)) {
            filePath = filePath.resolve(dir);
        }
        Path target = filePath.resolve(fileName).normalize();
        // 越界可能读到别的应用甚至 basePath 之外的文件，dir 与 fileName 里的 .. 都在这里被挡住
        if (!target.startsWith(rootPath)) {
            throw new IllegalArgumentException("非法的文件路径: "
                    + (StrUtil.isBlank(dir) ? fileName : dir + "/" + fileName));
        }
        return target;
    }

}
