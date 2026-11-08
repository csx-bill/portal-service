package com.bill.portal.scrm.mass.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bill.portal.scrm.mass.dto.WxMassBatchStatVO;
import com.bill.portal.scrm.mass.dto.WxMassTaskStatVO;
import com.bill.portal.scrm.mass.dto.WxMassUserStatVO;
import com.bill.portal.scrm.mass.entity.WxMassTargetLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;


@Mapper
public interface WxMassTargetLogMapper extends BaseMapper<WxMassTargetLog> {

    /**
     * 批量新增或更新
     * 基于 ON DUPLICATE KEY UPDATE 实现高性能写入
     * 触发条件基于联合唯一键 (tenant_id, task_id, invoke_id, user_id, target_id)
     */
    void batchUpsert(List<WxMassTargetLog> list);

    /**
     * 按任务聚合送达统计（全部批次合计）
     * @param taskIds 任务ID集合
     */
    List<WxMassTaskStatVO> statByTaskIds(@Param("taskIds") Collection<Long> taskIds);

    /**
     * 按批次（调用记录）聚合送达统计
     * @param taskId 任务ID
     */
    List<WxMassBatchStatVO> statByTaskGroupInvoke(@Param("taskId") Long taskId);

    /**
     * 按批次 + 成员聚合发送统计（成员发送明细）
     * @param taskId 任务ID
     */
    List<WxMassUserStatVO> statByTaskGroupInvokeUser(@Param("taskId") Long taskId);

}
