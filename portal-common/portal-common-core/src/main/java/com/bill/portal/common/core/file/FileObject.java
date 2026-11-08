package com.bill.portal.common.core.file;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.InputStream;

/**
 * 文件存储对象
 * <p>
 * 各存储实现（本地、S3 等）统一返回该对象，避免调用方依赖具体存储厂商的 SDK 类型。
 */
@Data
@AllArgsConstructor
public class FileObject {

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件大小，单位：字节
     */
    private long size;

    /**
     * 文件类型，可能为 null
     */
    private String contentType;

    /**
     * 文件内容，由调用方负责关闭
     */
    private InputStream content;

}
