package com.bill.portal.scrm.welcome.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bill.portal.scrm.welcome.dto.WxWelcomeMsgResp;
import com.bill.portal.scrm.welcome.entity.WxWelcomeMsg;
import com.bill.portal.scrm.welcome.req.WxWelcomeMsgQuery;
import com.bill.portal.scrm.welcome.req.WxWelcomeMsgSaveReq;

import java.util.List;

/** 企微欢迎语服务 */
public interface IWxWelcomeMsgService {

    /**
     * 分页查询主欢迎语（main_welcome_msg_id IS NULL）
     */
    IPage<WxWelcomeMsg> page(Page<WxWelcomeMsg> page, WxWelcomeMsgQuery query);

    /**
     * 查询欢迎语详情（含分时子项与绑定列表）
     */
    WxWelcomeMsgResp getWelcomeMsgById(Long id);

    /**
     * 保存欢迎语（id 为空新增，否则更新；分时子项与绑定整组替换）
     */
    void saveWelcomeMsg(WxWelcomeMsgSaveReq req);

    /**
     * 绑定冲突预检：返回与库中已有绑定时间重叠的冲突描述列表
     * （成员/部门「名称」已绑定欢迎语「标题」），空列表 = 无冲突
     */
    List<String> checkBindConflicts(WxWelcomeMsgSaveReq req);

    /**
     * 批量删除欢迎语（连同分时子项与绑定）
     */
    void delWelcomeMsgs(List<?> idList);

    /**
     * 添加客户事件触发的欢迎语发送（回调链路）。
     *
     * <p>按跟进成员/其所属部门的绑定解析欢迎语，按 sort 顺序匹配当前
     * 星期与时间段的分时规则，组装文本/图片/视频/链接后调用企微
     * send_welcome_msg；welcome_code 约20秒有效，图片/视频的 media_id
     * 过期时按 media_url 重传。无绑定/无命中规则/发送失败仅记录日志，
     * 不抛出异常（不得影响回调主流程）。
     *
     * @param corpId      企业ID
     * @param userId      跟进成员的企微 userid
     * @param welcomeCode 回调事件携带的欢迎语码
     */
    void sendForAddContact(String corpId, String userId, String welcomeCode);
}
