package com.bill.portal.system.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bill.portal.common.core.client.ClientContextHolder;
import com.bill.portal.common.core.file.FileProperties;
import com.bill.portal.common.core.file.FileStorageService;
import com.bill.portal.system.dto.SysFileQuery;
import com.bill.portal.system.dto.SysFileUploadDTO;
import com.bill.portal.system.entity.SysFile;
import com.bill.portal.system.mapper.SysFileMapper;
import com.bill.portal.system.service.ISysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements ISysFileService {

    private final FileStorageService fileStorageService;

    private final FileProperties properties;


    @Override
    public IPage<SysFile> pageFiles(IPage<SysFile> page, SysFileQuery query) {
        String clientId = ClientContextHolder.getClientId();
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<SysFile>()
                .eq(SysFile::getClientId, clientId)
                .eq(Objects.nonNull(query.getGroupId()), SysFile::getGroupId, query.getGroupId())
                .isNull(Boolean.TRUE.equals(query.getUngrouped()), SysFile::getGroupId)
                .like(StringUtils.hasText(query.getFileName()), SysFile::getFileName, query.getFileName())
                .orderByDesc(SysFile::getCreateTime);
        // 类型分类：image/video 按 MIME 前缀匹配，file 表示其余全部
        String category = query.getTypeCategory();
        if ("image".equals(category)) {
            wrapper.likeRight(SysFile::getType, "image/");
        } else if ("video".equals(category)) {
            wrapper.likeRight(SysFile::getType, "video/");
        } else if ("file".equals(category)) {
            wrapper.and(w -> w.notLikeRight(SysFile::getType, "image/")
                    .notLikeRight(SysFile::getType, "video/"));
        }
        IPage<SysFile> result = page(page, wrapper);
        return result;
    }


    @Override
    public SysFileUploadDTO uploadFile(MultipartFile file, String dir, Long groupId, String type) throws Exception {
        String clientId = ClientContextHolder.getClientId();
        String fileName = IdUtil.simpleUUID() + StrUtil.DOT + FileUtil.extName(file.getOriginalFilename());
        // 对原始文件名进行编码转换
        String originalFilename = new String(
                Objects.requireNonNull(file.getOriginalFilename()).getBytes(StandardCharsets.ISO_8859_1),
                StandardCharsets.UTF_8);

        fileStorageService.putObject(properties.getBucketName(), dir, fileName, file.getInputStream(), file.getSize(), file.getContentType());
        String accessUrl = fileStorageService.getAccessUrl(properties.getBucketName(), dir, fileName);

        SysFile sysFile = new SysFile();
        sysFile.setGroupId(groupId);
        sysFile.setFileName(fileName);
        sysFile.setBucketName(properties.getBucketName());
        sysFile.setDir(dir);
        sysFile.setOriginal(originalFilename);
        sysFile.setType(file.getContentType());
        sysFile.setFileSize(file.getSize());
        sysFile.setClientId(clientId);
        sysFile.setUrl(accessUrl);
        save(sysFile);

        SysFileUploadDTO result = new SysFileUploadDTO();
        result.setFileName(fileName);
        result.setUrl(accessUrl);
        return result;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatch(List<Long> ids, String clientId) {
        List<SysFile> files = listByIds(ids);
        fileStorageService.deleteObjects(properties.getBucketName(),clientId, files.stream()
                .map(SysFile::getFileName)
                .toList());
        return removeByIds(files.stream().map(SysFile::getId).toList());
    }

    @Override
    public boolean moveBatch(List<Long> ids, Long groupId, String clientId) {
        List<SysFile> files = listByIds(ids).stream()
                .filter(f -> clientId.equals(f.getClientId()))
                .toList();
        for (SysFile f : files) {
            // 注意：groupId 为 null 时需要显式更新为 NULL，
            // MyBatis-Plus 的 updateById 默认忽略 null 字段，这里用 UpdateWrapper
            update(new LambdaUpdateWrapper<SysFile>()
                    .eq(SysFile::getId, f.getId())
                    .set(SysFile::getGroupId, groupId));
        }
        return true;
    }
}
