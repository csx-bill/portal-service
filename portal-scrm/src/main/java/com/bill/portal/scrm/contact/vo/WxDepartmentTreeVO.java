package com.bill.portal.scrm.contact.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "部门树节点")
public class WxDepartmentTreeVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "企微部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "英文名称")
    private String nameEn;

    @Schema(description = "企微父部门ID")
    private Long parentId;

    @Schema(description = "部门排序值")
    private Long order;

    @Schema(description = "部门负责人userid列表")
    private List<String> departmentLeader;

    @Schema(description = "同步版本号")
    private Long syncVersion;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "修改人")
    private String updateBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "删除标记")
    private Integer delFlag;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "子部门列表")
    private List<WxDepartmentTreeVO> children;
}
