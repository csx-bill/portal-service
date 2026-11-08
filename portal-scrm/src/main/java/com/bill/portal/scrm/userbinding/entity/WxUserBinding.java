package com.bill.portal.scrm.userbinding.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户-企微成员绑定（纯关联表，物理删除）。
 * 数据权限链路：业务数据(归属企微userid) → 本表 → sys_user → sys_user_dept → sys_dept，
 * 由角色 data_scope 过滤；企微部门与系统部门互不同步、不参与数据权限。
 * 一个租户可授权多家企业，绑定按企业维度：租户内同一账号可分别绑定
 * 其在每家企业下的成员，唯一粒度 uk_tenant_user_corp / uk_tenant_corp_wxuser。
 */
@Data
@TableName(value = "wx_user_binding")
@Schema(description = "系统用户-企微成员绑定")
public class WxUserBinding implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "系统用户ID(sys_user.id)")
    private Long sysUserId;

    @Schema(description = "企业ID(随绑定的企微成员写入)")
    private String corpId;

    @Schema(description = "企微成员ID(wx_user.userid)")
    private String wxUserid;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
