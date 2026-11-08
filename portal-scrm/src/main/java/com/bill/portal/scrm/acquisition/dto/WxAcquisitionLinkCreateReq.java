package com.bill.portal.scrm.acquisition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "获客链接创建参数")
public class WxAcquisitionLinkCreateReq {

    @NotBlank
    @Schema(description = "企业ID")
    private String corpId;

    @NotBlank
    @Size(max = 30)
    @Schema(description = "链接名称，最长30字符")
    private String linkName;

    @Schema(description = "使用范围-成员ID列表")
    private List<String> rangeUserList;

    @Schema(description = "使用范围-部门ID列表")
    private List<Integer> rangeDepartmentList;

    @Schema(description = "是否跳过验证，默认true")
    private Boolean skipVerify;

    @Schema(description = "客户渠道（本地标记，sys_dict customer_channel 字典项值）")
    private String customerChannel;

    @Schema(description = "优先级类型: 1-企业内优先分配好友成员, 2-指定成员优先（仅部分经营类目企业支持）")
    private Integer priorityType;

    @Schema(description = "优先级成员ID列表（priorityType=2时必填，最多1000个）")
    private List<String> priorityUseridList;

    @Schema(description = "是否标记客户添加来源，默认true；仅「营销获客」应用生效")
    private Boolean markSource;
}
