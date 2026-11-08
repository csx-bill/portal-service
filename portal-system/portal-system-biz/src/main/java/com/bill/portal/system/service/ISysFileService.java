package com.bill.portal.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bill.portal.system.dto.SysFileQuery;
import com.bill.portal.system.dto.SysFileUploadDTO;
import com.bill.portal.system.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ISysFileService extends IService<SysFile> {

    /** 分页查询（支持分组/未分组/类型分类/文件名过滤） */
    IPage<SysFile> pageFiles(IPage<SysFile> page, SysFileQuery query);

    /**
     * 上传文件
     * @param file 文件流
     * @param dir 文件夹
     * @param groupId 分组ID
     * @param type 类型
     * @return 文件名与访问地址
     */
    SysFileUploadDTO uploadFile(MultipartFile file, String dir, Long groupId, String type) throws Exception;

    /** 批量删除（含物理文件） */
    boolean deleteBatch(List<Long> ids, String clientId);

    /** 批量移动分组（groupId 为 null 表示移到未分组） */
    boolean moveBatch(List<Long> ids, Long groupId, String clientId);
}
