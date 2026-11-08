package com.bill.portal.common.core.file.local;

import lombok.Data;
/**
 * 本地文件 配置信息
 * bucket 设置公共读权限
 */
@Data
public class LocalFileProperties {

    /**
     * 存储根目录，文件按 {basePath}/{bucket}/{clientId}/{dir}/{fileName} 落盘，
     * 访问地址为 /{bucket}/{clientId}/{dir}/{fileName}，由 nginx 把 /{bucket}/ 映射到本目录下的同名子目录
     */
    private String basePath;

}
