package com.bill.portal.scrm.userbinding.mapper;

import com.bill.portal.scrm.userbinding.dto.SysUserDirectoryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * sys_user 只读目录查询（同库跨模块访问，不引入 portal-system 依赖）。
 * 仅用于绑定列表回显账号姓名/登录名（selectByIds）；
 * 前端绑定抽屉的账号下拉直接调用 system 模块已有的 /system/user/page 接口，
 * scrm 不再重复暴露账号列表接口。
 * sys_user 不在租户表清单（全局表），租户归属由 sys_user_tenant 决定。
 */
@Mapper
public interface SysUserDirectoryMapper {

    /** 按ID集合查询（全局表，无租户条件） */
    @Select("""
            <script>
            SELECT id, username, name, mobile FROM sys_user
            WHERE id IN
            <foreach collection="ids" item="id" open="(" separator="," close=")">#{id}</foreach>
            </script>
            """)
    List<SysUserDirectoryDto> selectByIds(@Param("ids") Collection<Long> ids);
}
