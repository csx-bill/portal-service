package com.bill.portal.common.core.file;

import java.io.InputStream;
import java.util.Collection;

public interface FileStorageService {

    /**
     * 创建bucket
     * @param bucketName bucket名称
     */
    void createBucket(String bucketName);

    /**
     * 上传文件
     * @param bucketName 存储桶名称
     * @param dir 文件所在目录，可为空
     * @param fileName 文件名称
     * @param stream 文件流
     * @param contentLength 文件大小，单位：字节；未知时传 -1。
     *                      S3 等对象存储拿不到大小时，需要先把整个请求体缓冲到内存才能上传，
     *                      因此调用方能拿到文件大小时应尽量传入。
     *                      该值应取自服务端（如 MultipartFile.getSize()），不要使用前端传入的大小，
     *                      声明值小于实际内容会导致文件被静默截断
     * @param contextType 文件类型
     * @throws Exception
     */
    void putObject(String bucketName, String dir, String fileName, InputStream stream, long contentLength,
                   String contextType) throws Exception;


    /**
     * 获取文件访问地址，供客户端直接访问
     * <p>
     * 地址规则与读写文件时一致，保证返回的地址指向实际写入的对象；
     * 调用方需自行保证入参与写入时相同，否则会返回一个访问不到的地址
     * @param bucketName 存储桶名称
     * @param dir 文件所在目录，可为空
     * @param fileName 文件名称
     * @return 文件访问地址
     */
    String getAccessUrl(String bucketName, String dir, String fileName);


    /**
     * 删除文件，文件不存在时不报错
     * @param bucketName bucket名称
     * @param dir 文件所在目录，可为空
     * @param fileName 文件名称
     */
    void deleteObject(String bucketName, String dir, String fileName);


    /**
     * 批量删除文件，文件不存在时不报错
     * <p>
     * 不是原子操作：个别文件删除失败会抛出异常，已删除的不会回滚，
     * 由于删除是幂等的，重试安全
     * @param bucketName bucket名称
     * @param dir 文件所在目录，可为空
     * @param fileNames 文件名称集合
     */
    void deleteObjects(String bucketName, String dir, Collection<String> fileNames);
}
