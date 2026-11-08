package com.bill.portal.scrm.acquisition.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.chanjar.weixin.cp.bean.external.acquisition.WxCpCustomerAcquisitionInfo;

import java.util.List;

/**
 * 获客链接详情（SDK 扩展）。
 *
 * <p>SDK 4.8.3 的 {@link WxCpCustomerAcquisitionInfo} 未映射
 * priority_option 与 mark_source，同步回填时用本子类解析
 * customer_acquisition/get 的原始返回以保留完整配置。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WxCpCustomerAcquisitionExInfo extends WxCpCustomerAcquisitionInfo {

    /** 客户与成员关系绑定（可能为空：企业不支持或创建时未设置） */
    @SerializedName("priority_option")
    private PriorityOption priorityOption;

    /** 是否标记客户添加来源（未设置时为 null，语义同默认 true） */
    @SerializedName("mark_source")
    private Boolean markSource;

    @Data
    public static class PriorityOption {

        /** 1-全企业范围内优先分配给有好友关系的成员；2-指定范围内优先分配 */
        @SerializedName("priority_type")
        private Integer priorityType;

        /** priority_type=2 时的优先成员列表 */
        @SerializedName("priority_userid_list")
        private List<String> priorityUseridList;
    }
}
