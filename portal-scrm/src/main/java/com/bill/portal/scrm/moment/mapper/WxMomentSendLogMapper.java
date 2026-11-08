package com.bill.portal.scrm.moment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bill.portal.scrm.moment.dto.WxMomentBatchStatVO;
import com.bill.portal.scrm.moment.dto.WxMomentTaskStatVO;
import com.bill.portal.scrm.moment.dto.WxMomentUserStatVO;
import com.bill.portal.scrm.moment.entity.WxMomentSendLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface WxMomentSendLogMapper extends BaseMapper<WxMomentSendLog> {

    /**
     * 批量新增或更新（任务结果同步幂等写）
     * 触发条件基于联合唯一键 (tenant_id, invoke_id, user_id)
     */
    void batchUpsert(List<WxMomentSendLog> list);

    /**
     * 按任务聚合发表统计（全部批次合计）
     * @param taskIds 任务ID集合
     */
    List<WxMomentTaskStatVO> statByTaskIds(@Param("taskIds") Collection<Long> taskIds);

    /**
     * 按批次（调用记录）聚合发表统计
     * @param taskId 任务ID
     */
    List<WxMomentBatchStatVO> statByTaskGroupInvoke(@Param("taskId") Long taskId);

    /**
     * 按批次 + 成员聚合发表统计（成员发表明细）
     * @param taskId 任务ID
     */
    List<WxMomentUserStatVO> statByTaskGroupInvokeUser(@Param("taskId") Long taskId);

}
