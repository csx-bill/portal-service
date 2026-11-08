package com.bill.portal.common.core.file.s3;

import cn.hutool.core.util.StrUtil;
import com.bill.portal.common.core.client.ClientContextHolder;
import com.bill.portal.common.core.file.FileStorageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.ContentStreamProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class S3FileStorageServiceImpl implements FileStorageService {

    /**
     * S3 单次批量删除的 key 上限
     */
    private static final int DELETE_BATCH_SIZE = 100;

    /**
     * 未配置区域时使用的默认区域，与 {@link S3FileProperties#getRegion()} 保持一致
     */
    private static final String DEFAULT_REGION = "us-east-1";

    private final S3Client s3Client;

    private final S3FileProperties properties;

    @Override
    public void createBucket(String bucketName) {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException e) {
            log.debug("存储桶已存在: {}", bucketName);
        }
    }

    @Override
    public void putObject(String bucketName, String dir, String fileName, InputStream stream, long contentLength,
                          String contextType) {
        // 大小未知时交给 SDK 流式上传（aws-chunked 传输编码），SDK 会先把请求体缓冲到内存
        RequestBody body = contentLength >= 0
                ? RequestBody.fromInputStream(stream, contentLength)
                : RequestBody.fromContentProvider(ContentStreamProvider.fromInputStream(stream),
                        StrUtil.blankToDefault(contextType, "application/octet-stream"));
        doPutObject(bucketName, dir, fileName, body, contextType);
    }

    @Override
    public String getAccessUrl(String bucketName, String dir, String fileName) {
        String key = resolveKey(dir, fileName);
        String base = StrUtil.blankToDefault(properties.getPublicEndpoint(), properties.getEndpoint());
        // 未配置服务地址时按 AWS 默认域名拼接
        if (StrUtil.isBlank(base)) {
            String region = StrUtil.blankToDefault(properties.getRegion(), DEFAULT_REGION);
            return properties.isPathStyleAccess()
                    ? "https://s3." + region + ".amazonaws.com/" + bucketName + "/" + key
                    : "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
        }
        URI uri = URI.create(StrUtil.removeSuffix(base, "/"));
        if (properties.isPathStyleAccess()) {
            return uri + "/" + bucketName + "/" + key;
        }
        // virtual-host 风格：桶名作为子域
        String port = uri.getPort() == -1 ? "" : ":" + uri.getPort();
        return uri.getScheme() + "://" + bucketName + "." + uri.getHost() + port + "/" + key;
    }

    @Override
    public void deleteObject(String bucketName, String dir, String fileName) {
        // S3 删除不存在的对象同样返回成功，天然幂等
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(resolveKey(dir, fileName))
                .build());
    }

    @Override
    public void deleteObjects(String bucketName, String dir, Collection<String> fileNames) {
        List<String> keys = fileNames.stream().map(fileName -> resolveKey(dir, fileName)).toList();
        // S3 单次批量删除最多 1000 个 key，超出要分批
        for (int from = 0; from < keys.size(); from += DELETE_BATCH_SIZE) {
            List<String> batch = keys.subList(from, Math.min(from + DELETE_BATCH_SIZE, keys.size()));
            // quiet 模式只返回失败项
            DeleteObjectsResponse response = s3Client.deleteObjects(DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder()
                            .objects(batch.stream()
                                    .map(key -> ObjectIdentifier.builder().key(key).build())
                                    .toList())
                            .quiet(true)
                            .build())
                    .build());
            // 批量删除会部分成功，失败项必须暴露出来，否则调用方以为都删掉了
            if (!response.errors().isEmpty()) {
                String failed = response.errors().stream()
                        .map(error -> error.key() + "(" + error.code() + ")")
                        .collect(Collectors.joining(", "));
                throw new IllegalStateException("批量删除文件失败: " + failed);
            }
        }
    }

    private void doPutObject(String bucketName, String dir, String fileName, RequestBody body, String contextType) {
        // 检查存储桶是否存在，如果不存在则创建
        ensureBucket(bucketName);
        PutObjectRequest.Builder request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(resolveKey(dir, fileName));
        if (StrUtil.isNotBlank(contextType)) {
            request.contentType(contextType);
        }
        s3Client.putObject(request.build(), body);
    }

    /**
     * 对象 key 规则与本地存储保持一致：clientId/dir/fileName，clientId 与 dir 为空时跳过对应层级
     */
    private String resolveKey(String dir, String fileName) {
        if (StrUtil.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名称不能为空");
        }
        String key = StrUtil.isBlank(dir) ? fileName : dir + "/" + fileName;
        // 按应用隔离
        String clientId = ClientContextHolder.getClientId();
        return StrUtil.isBlank(clientId) ? key : clientId + "/" + key;
    }

    private void ensureBucket(String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (S3Exception e) {
            if (e.statusCode() != 404) {
                throw e;
            }
            log.info("存储桶不存在，自动创建: {}", bucketName);
            createBucket(bucketName);
        }
    }

}
