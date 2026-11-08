package com.bill.portal.common.core.scheduler.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bill.portal.common.core.scheduler.TaskInstance;
import com.bill.portal.common.core.scheduler.TaskRegistrar;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 基于 MyBatis-Plus 的 TaskRegistrar 抽象基类。
 *
 * 子类只需提供：BaseMapper + 实体类型 + 执行器。
 * 默认列名：id / status / scheduled_time / execute_type。
 * 如果表字段名不同，覆写对应的 colXxx() 方法。
 *
 * @param <T> 实体类型（需实现 TaskInstance，且对应 MyBatis-Plus 表映射）
 */
public abstract class AbstractMybatisTaskRegistrar<T extends TaskInstance>
        implements TaskRegistrar<T> {

    /** 子类提供 MyBatis-Plus Mapper */
    protected abstract BaseMapper<T> mapper();

    // ────── 列名（子类可按需覆写） ──────

    protected String colId()            { return "id"; }
    protected String colStatus()        { return "status"; }
    protected String colScheduledTime() { return "scheduled_time"; }
    protected String colExecuteType()   { return "execute_type"; }

    // ────── 加载 ──────

    /** 查询所有启用的周期任务（status=1 AND execute_type>=2） */
    @Override
    public List<T> loadActiveTasks() {
        return mapper().selectList(
            new QueryWrapper<T>()
                .eq(colStatus(), 1)
                .ge(colExecuteType(), 2)
        );
    }

    /** 查询已到期但未执行的任务 */
    @Override
    public List<T> loadDueTasks() {
        return mapper().selectList(
            new QueryWrapper<T>()
                .eq(colStatus(), 1)
                .le(colScheduledTime(), LocalDateTime.now())
                .orderByAsc(colScheduledTime())
        );
    }

    /** 按主键加载 */
    @Override
    public T loadById(Long taskId) {
        return mapper().selectById(taskId);
    }

    // ────── 持久化 ──────

    /** CAS 抢锁：原子地将 status 从 1 改为 2 */
    @Override
    public boolean casLock(Long taskId) {
        return mapper().update(null,
            new UpdateWrapper<T>()
                .eq(colId(), taskId)
                .eq(colStatus(), 1)
                .set(colStatus(), 2)
        ) > 0;
    }

    /** 执行成功：更新下次执行时间 + 恢复启用 */
    @Override
    public void onSuccess(Long taskId, LocalDateTime nextTime) {
        mapper().update(null,
            new UpdateWrapper<T>()
                .eq(colId(), taskId)
                .set(colStatus(), 1)
                .set(colScheduledTime(), nextTime)
        );
    }

    /** 执行失败：恢复为启用，等待重试 */
    @Override
    public void onFailure(Long taskId) {
        mapper().update(null,
            new UpdateWrapper<T>()
                .eq(colId(), taskId)
                .set(colStatus(), 1)
        );
    }

    /** 任务完成（无下次执行）：标记为已完成 */
    @Override
    public void onComplete(Long taskId) {
        mapper().update(null,
            new UpdateWrapper<T>()
                .eq(colId(), taskId)
                .set(colStatus(), 3)
        );
    }
}
