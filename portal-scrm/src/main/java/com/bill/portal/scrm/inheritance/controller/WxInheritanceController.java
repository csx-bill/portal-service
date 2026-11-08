package com.bill.portal.scrm.inheritance.controller;

import com.bill.portal.common.core.model.R;
import com.bill.portal.scrm.inheritance.dto.CustomerTransferReqDTO;
import com.bill.portal.scrm.inheritance.dto.CustomerTransferResultDTO;
import com.bill.portal.scrm.inheritance.dto.GroupChatTransferReqDTO;
import com.bill.portal.scrm.inheritance.dto.GroupChatTransferRespDTO;
import com.bill.portal.scrm.inheritance.dto.InheritUnassignedQueryDTO;
import com.bill.portal.scrm.inheritance.dto.TransferResultQueryDTO;
import com.bill.portal.scrm.inheritance.dto.TransferResultRespDTO;
import com.bill.portal.scrm.inheritance.dto.UnassignedListRespDTO;
import com.bill.portal.scrm.inheritance.service.IWxInheritanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 企微客户继承（在职继承 + 离职继承）
 *
 * <p>薄代理企微外部联系人接口：分配结果 errcode=0 仅代表已发起接替流程
 * （24小时后自动接替），最终结果以接替状态查询为准。
 */
@Tag(name = "企微客户继承")
@RestController
@RequestMapping("/scrm/inheritance")
@AllArgsConstructor
public class WxInheritanceController {

    private final IWxInheritanceService wxInheritanceService;

    @PostMapping("/unassigned/list")
    @Operation(summary = "待分配的离职成员客户列表", description = "游标分页，代理企微 /externalcontact/get_unassigned_list")
    public R<UnassignedListRespDTO> unassignedList(@RequestBody InheritUnassignedQueryDTO query) {
        return R.success(wxInheritanceService.unassignedList(query));
    }

    @PostMapping("/onjob/customer/transfer")
    @SaCheckPermission("inheritance:transfer")
    @Operation(summary = "在职继承：分配在职成员的客户", description = "代理企微 /externalcontact/transfer_customer，24小时后自动接替")
    public R<List<CustomerTransferResultDTO>> transferOnjobCustomer(@RequestBody CustomerTransferReqDTO req) {
        return R.success(wxInheritanceService.transferOnjobCustomer(req));
    }

    @PostMapping("/onjob/customer/transfer/result")
    @Operation(summary = "在职继承：查询客户接替状态", description = "代理企微 /externalcontact/transfer_result，游标分页")
    public R<TransferResultRespDTO> onjobTransferResult(@RequestBody TransferResultQueryDTO query) {
        return R.success(wxInheritanceService.onjobTransferResult(query));
    }

    @PostMapping("/onjob/groupchat/transfer")
    @SaCheckPermission("inheritance:transfer")
    @Operation(summary = "在职继承：分配在职成员的客户群", description = "代理企微 /externalcontact/groupchat/onjob_transfer")
    public R<GroupChatTransferRespDTO> transferOnjobGroupChat(@RequestBody GroupChatTransferReqDTO req) {
        return R.success(wxInheritanceService.transferOnjobGroupChat(req));
    }

    @PostMapping("/resigned/customer/transfer")
    @SaCheckPermission("inheritance:transfer")
    @Operation(summary = "离职继承：分配离职成员的客户", description = "代理企微 /externalcontact/resigned/transfer_customer，24小时后自动接替")
    public R<List<CustomerTransferResultDTO>> transferResignedCustomer(@RequestBody CustomerTransferReqDTO req) {
        return R.success(wxInheritanceService.transferResignedCustomer(req));
    }

    @PostMapping("/resigned/customer/transfer/result")
    @Operation(summary = "离职继承：查询客户接替状态", description = "代理企微 /externalcontact/resigned/transfer_result，游标分页")
    public R<TransferResultRespDTO> resignedTransferResult(@RequestBody TransferResultQueryDTO query) {
        return R.success(wxInheritanceService.resignedTransferResult(query));
    }

    @PostMapping("/resigned/groupchat/transfer")
    @SaCheckPermission("inheritance:transfer")
    @Operation(summary = "离职继承：分配离职成员的客户群", description = "代理企微 /externalcontact/groupchat/transfer")
    public R<GroupChatTransferRespDTO> transferResignedGroupChat(@RequestBody GroupChatTransferReqDTO req) {
        return R.success(wxInheritanceService.transferResignedGroupChat(req));
    }
}
