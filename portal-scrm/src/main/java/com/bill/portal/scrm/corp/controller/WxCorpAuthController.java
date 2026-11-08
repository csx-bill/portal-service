package com.bill.portal.scrm.corp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bill.portal.common.core.model.R;
import com.bill.portal.scrm.corp.dto.WxCorpAuthQuery;
import com.bill.portal.scrm.corp.entity.WxCorpAuth;
import com.bill.portal.scrm.corp.service.IWxCorpAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 企微企业授权管理接口
 */
@RestController
@RequestMapping("/scrm/corp")
@Tag(name = "企微企业授权管理")
@AllArgsConstructor
public class WxCorpAuthController {

    private final IWxCorpAuthService wxCorpAuthService;

    @GetMapping("/list")
    @Operation(summary = "列表查询")
    public R<List<WxCorpAuth>> list(@ParameterObject @Valid WxCorpAuthQuery query) {
        LambdaQueryWrapper<WxCorpAuth> wrapper = Wrappers.<WxCorpAuth>lambdaQuery()
                .eq(StringUtils.hasText(query.getCorpId()), WxCorpAuth::getCorpId, query.getCorpId())
                .like(StringUtils.hasText(query.getCorpName()), WxCorpAuth::getCorpName, query.getCorpName())
                .orderByAsc(WxCorpAuth::getId);
        return R.success(wxCorpAuthService.list(wrapper));
    }

}
