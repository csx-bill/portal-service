package com.bill.portal.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bill.portal.common.core.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统文件实体（文件元数据，上传管道由存储侧写入）
 */
@Data
@TableName(value = "sys_file", autoResultMap = true)
@Schema(description = "系统文件实体")
public class SysFile extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "所属分组ID")
    private Long groupId;

    @Schema(description = "文件名（存储侧）")
    private String fileName;

    @Schema(description = "存储桶名")
    private String bucketName;

    @Schema(description = "存储目录")
    private String dir;

    @Schema(description = "原始文件名")
    private String original;

    @Schema(description = "文件类型")
    private String type;

    @Schema(description = "文件大小（字节）")
    private Long fileSize;

    @Schema(description = "客户端ID")
    private String clientId;

    @Schema(description = "文件访问地址")
    private String url;

    @Schema(description = "租户ID")
    private Long tenantId;
}
