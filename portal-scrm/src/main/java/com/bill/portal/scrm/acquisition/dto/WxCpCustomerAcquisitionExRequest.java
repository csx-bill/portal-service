package com.bill.portal.scrm.acquisition.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.chanjar.weixin.cp.bean.external.acquisition.WxCpCustomerAcquisitionRequest;

import java.util.List;

/**
 * 获客链接创建/编辑请求（SDK 扩展）。
 *
 * <p>SDK 4.8.3 的 {@link WxCpCustomerAcquisitionRequest} 尚未支持
 * priority_option（客户与成员关系绑定）与 mark_source 字段；
 * 其 toJson() 用 Gson 按运行时类型序列化，子类字段原样输出，
 * 直接传入 customerAcquisitionLinkCreate/Update 即可生效。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WxCpCustomerAcquisitionExRequest extends WxCpCustomerAcquisitionRequest {

    /** 客户与成员关系绑定：1-企业内优先好友成员，2-指定成员优先（仅部分经营类目企业支持） */
    @SerializedName("priority_option")
    private PriorityOption priorityOption;

    /** 是否标记客户添加来源，默认 true；仅「营销获客」应用生效 */
    @SerializedName("mark_source")
    private Boolean markSource;

    @Data
    public static class PriorityOption {

        /** 1-全企业范围内优先分配给有好友关系的成员；2-指定范围内优先分配 */
        @SerializedName("priority_type")
        private Integer priorityType;

        /** priority_type=2 时必填，最多1000个 */
        @SerializedName("priority_userid_list")
        private List<String> priorityUseridList;
    }
}
