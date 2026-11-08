package com.bill.portal.scrm.acquisition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "获客链接查询参数")
public class WxAcquisitionLinkQuery {

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "链接名称（模糊搜索）")
    private String linkName;

    @Schema(description = "按使用范围成员过滤（企微 userid，命中 range_user_list）")
    private String rangeUserid;

    @Schema(description = "按使用范围部门过滤（企微部门ID，命中 range_department_list）")
    private Integer rangeDeptId;
}
