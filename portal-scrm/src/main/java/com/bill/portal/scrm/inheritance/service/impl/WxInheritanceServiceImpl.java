package com.bill.portal.scrm.inheritance.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bill.portal.common.core.exception.CheckedException;
import com.bill.portal.scrm.corp.strategy.WxCorpApiStrategy;
import com.bill.portal.scrm.customer.entity.WxCustomer;
import com.bill.portal.scrm.customer.mapper.WxCustomerMapper;
import com.bill.portal.scrm.inheritance.dto.CustomerTransferReqDTO;
import com.bill.portal.scrm.inheritance.dto.CustomerTransferResultDTO;
import com.bill.portal.scrm.inheritance.dto.GroupChatTransferFailedDTO;
import com.bill.portal.scrm.inheritance.dto.GroupChatTransferReqDTO;
import com.bill.portal.scrm.inheritance.dto.GroupChatTransferRespDTO;
import com.bill.portal.scrm.inheritance.dto.InheritUnassignedQueryDTO;
import com.bill.portal.scrm.inheritance.dto.TransferResultItemDTO;
import com.bill.portal.scrm.inheritance.dto.TransferResultQueryDTO;
import com.bill.portal.scrm.inheritance.dto.TransferResultRespDTO;
import com.bill.portal.scrm.inheritance.dto.UnassignedItemDTO;
import com.bill.portal.scrm.inheritance.dto.UnassignedListRespDTO;
import com.bill.portal.scrm.inheritance.service.IWxInheritanceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.WxCpExternalContactService;
import me.chanjar.weixin.cp.bean.external.WxCpUserExternalGroupChatTransferResp;
import me.chanjar.weixin.cp.bean.external.WxCpUserExternalUnassignList;
import me.chanjar.weixin.cp.bean.external.WxCpUserTransferCustomerReq;
import me.chanjar.weixin.cp.bean.external.WxCpUserTransferCustomerResp;
import me.chanjar.weixin.cp.bean.external.WxCpUserTransferResultResp;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户继承服务实现（在职继承 + 离职继承）。
 *
 * <p>薄代理层：入参校验后调用企微接口，返回逐客户/逐群的结果明细。
 * 注意分配接口 errcode=0 仅代表已发起接替流程（24 小时后自动接替），
 * 最终结果需通过接替状态查询接口确认。
 */
@Slf4j
@Service
@AllArgsConstructor
public class WxInheritanceServiceImpl implements IWxInheritanceService {

    private final WxCorpApiStrategy wxCorpApiStrategy;

    private final WxCustomerMapper wxCustomerMapper;

    private WxCpExternalContactService externalContactService(String corpId) {
        return wxCorpApiStrategy.getWxCpService(corpId).getExternalContactService();
    }

    @Override
    public UnassignedListRespDTO unassignedList(InheritUnassignedQueryDTO query) {
        if (StringUtils.isBlank(query.getCorpId())) {
            throw new CheckedException("企业不能为空");
        }
        try {
            WxCpUserExternalUnassignList list = externalContactService(query.getCorpId())
                    .listUnassignedList(null, query.getCursor(), query.getPageSize());
            UnassignedListRespDTO resp = new UnassignedListRespDTO();
            List<UnassignedItemDTO> items = new ArrayList<>();
            for (WxCpUserExternalUnassignList.UnassignInfo info : list.getUnassignInfos()) {
                UnassignedItemDTO item = new UnassignedItemDTO();
                item.setHandoverUserid(info.getHandoverUserid());
                item.setExternalUserid(info.getExternalUserid());
                item.setDimissionTime(info.getDimissionTime());
                items.add(item);
            }
            resp.setList(items);
            resp.setIsLast(list.isLast());
            resp.setNextCursor(StringUtils.defaultString(list.getNextCursor()));
            return resp;
        } catch (WxErrorException e) {
            log.warn("[inheritance] 待分配离职成员列表查询失败：{}", e.getMessage(), e);
            throw new CheckedException("查询待分配列表失败：" + e.getError().getErrorMsg());
        }
    }

    @Override
    public List<CustomerTransferResultDTO> transferOnjobCustomer(CustomerTransferReqDTO req) {
        validateTransfer(req);
        try {
            WxCpUserTransferCustomerResp resp = externalContactService(req.getCorpId())
                    .transferCustomer(buildTransferReq(req));
            List<CustomerTransferResultDTO> results = toCustomerResults(resp);
            annotateHandover(req, results, "在职继承");
            return results;
        } catch (WxErrorException e) {
            log.warn("[inheritance] 在职客户分配失败：{}", e.getMessage(), e);
            throw new CheckedException("分配失败：" + e.getError().getErrorMsg());
        }
    }

    @Override
    public TransferResultRespDTO onjobTransferResult(TransferResultQueryDTO query) {
        try {
            WxCpUserTransferResultResp resp = externalContactService(query.getCorpId())
                    .transferResult(query.getHandoverUserid(), query.getTakeoverUserid(),
                            StringUtils.defaultString(query.getCursor()));
            return toTransferResultResp(resp);
        } catch (WxErrorException e) {
            log.warn("[inheritance] 在职客户接替状态查询失败：{}", e.getMessage(), e);
            throw new CheckedException("查询接替状态失败：" + e.getError().getErrorMsg());
        }
    }

    @Override
    public GroupChatTransferRespDTO transferOnjobGroupChat(GroupChatTransferReqDTO req) {
        validateGroupChatTransfer(req);
        try {
            WxCpUserExternalGroupChatTransferResp resp = externalContactService(req.getCorpId())
                    .onjobTransferGroupChat(req.getChatIdList().toArray(new String[0]), req.getNewOwner());
            return toGroupChatResp(resp);
        } catch (WxErrorException e) {
            log.warn("[inheritance] 在职客群转接失败：{}", e.getMessage(), e);
            throw new CheckedException("客户群转接失败：" + e.getError().getErrorMsg());
        }
    }

    @Override
    public List<CustomerTransferResultDTO> transferResignedCustomer(CustomerTransferReqDTO req) {
        validateTransfer(req);
        try {
            WxCpUserTransferCustomerResp resp = externalContactService(req.getCorpId())
                    .resignedTransferCustomer(buildTransferReq(req));
            List<CustomerTransferResultDTO> results = toCustomerResults(resp);
            annotateHandover(req, results, "离职继承");
            return results;
        } catch (WxErrorException e) {
            log.warn("[inheritance] 离职客户分配失败：{}", e.getMessage(), e);
            throw new CheckedException("分配失败：" + e.getError().getErrorMsg());
        }
    }

    @Override
    public TransferResultRespDTO resignedTransferResult(TransferResultQueryDTO query) {
        try {
            WxCpUserTransferResultResp resp = externalContactService(query.getCorpId())
                    .resignedTransferResult(query.getHandoverUserid(), query.getTakeoverUserid(),
                            StringUtils.defaultString(query.getCursor()));
            return toTransferResultResp(resp);
        } catch (WxErrorException e) {
            log.warn("[inheritance] 离职客户接替状态查询失败：{}", e.getMessage(), e);
            throw new CheckedException("查询接替状态失败：" + e.getError().getErrorMsg());
        }
    }

    @Override
    public GroupChatTransferRespDTO transferResignedGroupChat(GroupChatTransferReqDTO req) {
        validateGroupChatTransfer(req);
        try {
            WxCpUserExternalGroupChatTransferResp resp = externalContactService(req.getCorpId())
                    .transferGroupChat(req.getChatIdList().toArray(new String[0]), req.getNewOwner());
            return toGroupChatResp(resp);
        } catch (WxErrorException e) {
            log.warn("[inheritance] 离职客群转接失败：{}", e.getMessage(), e);
            throw new CheckedException("客户群转接失败：" + e.getError().getErrorMsg());
        }
    }

    // ==================== 私有方法 ====================

    private WxCpUserTransferCustomerReq buildTransferReq(CustomerTransferReqDTO req) {
        WxCpUserTransferCustomerReq transferReq = new WxCpUserTransferCustomerReq();
        transferReq.setHandOverUserid(req.getHandoverUserid());
        transferReq.setTakeOverUserid(req.getTakeoverUserid());
        transferReq.setExternalUserid(req.getExternalUseridList());
        if (StringUtils.isNotBlank(req.getTransferMsg())) {
            transferReq.setTransferMsg(req.getTransferMsg());
        }
        return transferReq;
    }

    private void validateTransfer(CustomerTransferReqDTO req) {
        if (StringUtils.isBlank(req.getCorpId())) {
            throw new CheckedException("企业不能为空");
        }
        if (StringUtils.isBlank(req.getHandoverUserid())) {
            throw new CheckedException("原跟进成员不能为空");
        }
        if (StringUtils.isBlank(req.getTakeoverUserid())) {
            throw new CheckedException("接替成员不能为空");
        }
        if (req.getHandoverUserid().equals(req.getTakeoverUserid())) {
            throw new CheckedException("原跟进成员与接替成员不能相同");
        }
        if (CollectionUtils.isEmpty(req.getExternalUseridList())) {
            throw new CheckedException("客户列表不能为空");
        }
        if (req.getExternalUseridList().size() > 100) {
            throw new CheckedException("每次最多分配100个客户");
        }
    }

    private void validateGroupChatTransfer(GroupChatTransferReqDTO req) {
        if (StringUtils.isBlank(req.getCorpId())) {
            throw new CheckedException("企业不能为空");
        }
        if (CollectionUtils.isEmpty(req.getChatIdList())) {
            throw new CheckedException("客户群列表不能为空");
        }
        if (req.getChatIdList().size() > 100) {
            throw new CheckedException("每次最多转接100个客户群");
        }
        if (StringUtils.isBlank(req.getNewOwner())) {
            throw new CheckedException("新群主不能为空");
        }
    }

    private List<CustomerTransferResultDTO> toCustomerResults(WxCpUserTransferCustomerResp resp) {
        List<CustomerTransferResultDTO> results = new ArrayList<>();
        for (WxCpUserTransferCustomerResp.TransferCustomer c : resp.getCustomer()) {
            CustomerTransferResultDTO item = new CustomerTransferResultDTO();
            item.setExternalUserid(c.getExternalUserid());
            item.setErrcode(c.getErrcode());
            results.add(item);
        }
        return results;
    }

    private TransferResultRespDTO toTransferResultResp(WxCpUserTransferResultResp resp) {
        TransferResultRespDTO result = new TransferResultRespDTO();
        result.setNextCursor(StringUtils.defaultString(resp.getNextCursor()));
        List<TransferResultItemDTO> items = new ArrayList<>();
        for (WxCpUserTransferResultResp.TransferResult item : resp.getCustomer()) {
            TransferResultItemDTO dto = new TransferResultItemDTO();
            dto.setExternalUserid(item.getExternalUserid());
            dto.setStatus(statusToCode(item.getStatus()));
            dto.setTakeoverTime(item.getTakeOverTime());
            items.add(dto);
        }
        result.setCustomer(items);
        return result;
    }

    private GroupChatTransferRespDTO toGroupChatResp(WxCpUserExternalGroupChatTransferResp resp) {
        GroupChatTransferRespDTO result = new GroupChatTransferRespDTO();
        List<GroupChatTransferFailedDTO> failedList = new ArrayList<>();
        for (WxCpUserExternalGroupChatTransferResp.GroupChatFailedTransfer failed : resp.getFailedChatList()) {
            GroupChatTransferFailedDTO item = new GroupChatTransferFailedDTO();
            item.setChatId(failed.getChatId());
            item.setErrcode(failed.getErrcode());
            item.setErrmsg(failed.getErrmsg());
            failedList.add(item);
        }
        result.setFailedChatList(failedList);
        return result;
    }

    /** 接替状态枚举 → 官方状态码：1-接替完毕 2-等待接替 3-客户拒绝 4-上限 5-无记录 */
    private Integer statusToCode(WxCpUserTransferResultResp.STATUS status) {
        if (status == null) {
            return null;
        }
        switch (status) {
            case COMPLETE:
                return 1;
            case WAITING:
                return 2;
            case REFUSED:
                return 3;
            case LIMIT:
                return 4;
            case NORECORD:
                return 5;
            default:
                return null;
        }
    }

    /**
     * 发起接替成功后，在原成员的关系行上记录交接痕迹（仅写流失原因注释，
     * 不改流失标记）。注释携带继承类型，兼作对账任务的分流标记；
     * 后续 del_follow_user/add_external_contact 回调会把原因覆盖为最终
     * 状态；若客户拒绝接替，transfer_fail 处理会清除该注释。
     */
    private void annotateHandover(CustomerTransferReqDTO req, List<CustomerTransferResultDTO> results, String inheritType) {
        for (CustomerTransferResultDTO result : results) {
            if (result.getErrcode() == null || result.getErrcode() != 0) {
                continue;
            }
            wxCustomerMapper.update(null, Wrappers.<WxCustomer>lambdaUpdate()
                    .eq(WxCustomer::getCorpId, req.getCorpId())
                    .eq(WxCustomer::getExternalUserId, result.getExternalUserid())
                    .eq(WxCustomer::getUserId, req.getHandoverUserid())
                    .set(WxCustomer::getLostReason,
                            "客户交接给成员 " + req.getTakeoverUserid() + "（" + inheritType + "已发起）")
                    .set(WxCustomer::getHandoverToUserid, req.getTakeoverUserid())
                    .set(WxCustomer::getHandoverTime, LocalDateTime.now())
                    .set(WxCustomer::getUpdateTime, LocalDateTime.now()));
            log.info("[inheritance] 客户交接已发起并记录：corpId={}, externalUserId={}, " +
                    "原成员={}, 接替成员={}, 类型={}", req.getCorpId(), result.getExternalUserid(),
                    req.getHandoverUserid(), req.getTakeoverUserid(), inheritType);
        }
    }
}
