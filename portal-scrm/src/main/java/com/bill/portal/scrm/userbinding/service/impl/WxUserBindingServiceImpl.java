package com.bill.portal.scrm.userbinding.service.impl;

import com.bill.portal.common.core.exception.CheckedException;
import com.bill.portal.common.core.tenant.TenantContextHolder;
import com.bill.portal.scrm.contact.entity.WxUser;
import com.bill.portal.scrm.contact.mapper.WxUserMapper;
import com.bill.portal.scrm.userbinding.dto.SysUserDirectoryDto;
import com.bill.portal.scrm.userbinding.dto.WxUserBindingSaveReq;
import com.bill.portal.scrm.userbinding.entity.WxUserBinding;
import com.bill.portal.scrm.userbinding.mapper.SysUserDirectoryMapper;
import com.bill.portal.scrm.userbinding.mapper.WxUserBindingMapper;
import com.bill.portal.scrm.userbinding.service.IWxUserBindingService;
import com.bill.portal.scrm.userbinding.vo.WxUserBindingVo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 系统用户-企微成员绑定实现。
 * 租户条件（tenant_id）由 TenantLineInnerInterceptor 对登记表自动注入，
 * 业务代码不手工拼租户条件；sys_user 为全局表，经只读目录 Mapper 访问。
 */
@Service
@RequiredArgsConstructor
public class WxUserBindingServiceImpl implements IWxUserBindingService {

    private final WxUserBindingMapper bindingMapper;
    private final WxUserMapper wxUserMapper;
    private final SysUserDirectoryMapper sysUserDirectoryMapper;

    @Override
    public List<WxUserBindingVo> list() {
        List<WxUserBinding> bindings = bindingMapper.selectList(Wrappers.emptyWrapper());
        if (bindings.isEmpty()) {
            return List.of();
        }

        Map<String, WxUser> wxUsers = wxUserMapper
                .selectList(Wrappers.<WxUser>lambdaQuery()
                        .in(WxUser::getUserId, bindings.stream().map(WxUserBinding::getWxUserid).toList()))
                .stream()
                .collect(Collectors.toMap(WxUser::getUserId, Function.identity(), (a, b) -> a));

        Map<Long, SysUserDirectoryDto> sysUsers = sysUserDirectoryMapper
                .selectByIds(bindings.stream().map(WxUserBinding::getSysUserId).toList())
                .stream()
                .collect(Collectors.toMap(SysUserDirectoryDto::getId, Function.identity(), (a, b) -> a));

        return bindings.stream().map(binding -> {
            WxUserBindingVo vo = new WxUserBindingVo();
            vo.setId(binding.getId());
            vo.setSysUserId(binding.getSysUserId());
            SysUserDirectoryDto sysUser = sysUsers.get(binding.getSysUserId());
            vo.setSysUserName(sysUser == null ? null : sysUser.getName());
            vo.setSysUserUsername(sysUser == null ? null : sysUser.getUsername());
            vo.setCorpId(binding.getCorpId());
            vo.setWxUserid(binding.getWxUserid());
            WxUser wxUser = wxUsers.get(binding.getWxUserid());
            vo.setWxUserName(wxUser == null ? null : wxUser.getName());
            vo.setCreateTime(binding.getCreateTime());
            return vo;
        }).toList();
    }

    @Override
    public void bind(WxUserBindingSaveReq req) {
        Long tenantId = requireTenantId();
        String wxUserid = req.getWxUserid().trim();

        // 成员必须存在于当前租户的指定企业（wx_user 已登记租户表，查询自动带租户条件）
        WxUser wxUser = wxUserMapper.selectOne(Wrappers.<WxUser>lambdaQuery()
                .eq(WxUser::getCorpId, req.getCorpId())
                .eq(WxUser::getUserId, wxUserid)
                .last("LIMIT 1"));
        if (wxUser == null) {
            throw new CheckedException("企微成员不存在");
        }

        // 同企业内，目标成员已被其他账号绑定 → 拒绝
        WxUserBinding memberBound = bindingMapper.selectOne(Wrappers.<WxUserBinding>lambdaQuery()
                .eq(WxUserBinding::getCorpId, req.getCorpId())
                .eq(WxUserBinding::getWxUserid, wxUserid)
                .ne(WxUserBinding::getSysUserId, req.getSysUserId())
                .last("LIMIT 1"));
        if (memberBound != null) {
            throw new CheckedException("该企微成员已绑定其他系统账号");
        }

        // 同一账号在同一企业重复绑定 → 换绑（改绑目标成员）；
        // 账号在其他企业已有绑定 → 直接新增一条（多企业分别绑定）
        WxUserBinding existing = getBySysUserAndCorp(req.getSysUserId(), req.getCorpId());
        if (existing != null) {
            existing.setWxUserid(wxUserid);
            bindingMapper.updateById(existing);
            return;
        }

        WxUserBinding binding = new WxUserBinding();
        binding.setTenantId(tenantId);
        binding.setSysUserId(req.getSysUserId());
        binding.setCorpId(wxUser.getCorpId());
        binding.setWxUserid(wxUserid);
        binding.setCreateTime(LocalDateTime.now());
        bindingMapper.insert(binding);
    }

    @Override
    public void unbind(Long id) {
        bindingMapper.deleteById(id);
    }

    @Override
    public List<WxUserBinding> listBySysUserId(Long sysUserId) {
        return bindingMapper.selectList(Wrappers.<WxUserBinding>lambdaQuery()
                .eq(WxUserBinding::getSysUserId, sysUserId));
    }

    /** 账号在某企业下的绑定（无则 null） */
    private WxUserBinding getBySysUserAndCorp(Long sysUserId, String corpId) {
        return bindingMapper.selectOne(Wrappers.<WxUserBinding>lambdaQuery()
                .eq(WxUserBinding::getSysUserId, sysUserId)
                .eq(WxUserBinding::getCorpId, corpId)
                .last("LIMIT 1"));
    }

    private Long requireTenantId() {
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new CheckedException("租户上下文缺失，无法绑定");
        }
        return tenantId;
    }
}
