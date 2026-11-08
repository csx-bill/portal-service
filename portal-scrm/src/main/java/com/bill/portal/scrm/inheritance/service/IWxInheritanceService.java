package com.bill.portal.scrm.inheritance.service;

import com.bill.portal.scrm.inheritance.dto.CustomerTransferReqDTO;
import com.bill.portal.scrm.inheritance.dto.CustomerTransferResultDTO;
import com.bill.portal.scrm.inheritance.dto.GroupChatTransferFailedDTO;
import com.bill.portal.scrm.inheritance.dto.GroupChatTransferReqDTO;
import com.bill.portal.scrm.inheritance.dto.InheritUnassignedQueryDTO;
import com.bill.portal.scrm.inheritance.dto.TransferResultQueryDTO;
import com.bill.portal.scrm.inheritance.dto.GroupChatTransferRespDTO;
import com.bill.portal.scrm.inheritance.dto.TransferResultRespDTO;
import com.bill.portal.scrm.inheritance.dto.UnassignedListRespDTO;

import java.util.List;

/**
 * 客户继承服务（在职继承 + 离职继承），代理企微外部联系人相关接口
 */
public interface IWxInheritanceService {

    /**
     * 待分配的离职成员客户列表（游标分页）
     */
    UnassignedListRespDTO unassignedList(InheritUnassignedQueryDTO query);

    /**
     * 在职继承：分配在职成员的客户（24小时后自动接替）
     */
    List<CustomerTransferResultDTO> transferOnjobCustomer(CustomerTransferReqDTO req);

    /**
     * 在职继承：查询客户接替状态（游标分页）
     */
    TransferResultRespDTO onjobTransferResult(TransferResultQueryDTO query);

    /**
     * 在职继承：分配在职成员的客户群
     */
    GroupChatTransferRespDTO transferOnjobGroupChat(GroupChatTransferReqDTO req);

    /**
     * 离职继承：分配离职成员的客户（24小时后自动接替）
     */
    List<CustomerTransferResultDTO> transferResignedCustomer(CustomerTransferReqDTO req);

    /**
     * 离职继承：查询客户接替状态（游标分页）
     */
    TransferResultRespDTO resignedTransferResult(TransferResultQueryDTO query);

    /**
     * 离职继承：分配离职成员的客户群
     */
    GroupChatTransferRespDTO transferResignedGroupChat(GroupChatTransferReqDTO req);
}
