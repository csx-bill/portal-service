package com.bill.portal.scrm.datascope;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bill.portal.common.core.datascope.DataScopeHandler;
import com.bill.portal.common.core.datascope.DataScopeContextHolder;
import com.bill.portal.common.core.datascope.DataScopeOwner;
import com.bill.portal.common.core.datascope.DataScopeScopeResolver;
import com.bill.portal.scrm.userbinding.entity.WxUserBinding;
import com.bill.portal.scrm.userbinding.mapper.WxUserBindingMapper;
import com.bill.portal.common.core.datascope.DataScopeView;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SCRM 数据权限 Handler（数据权限链路的模块实现）。
 *
 * 管辖范围：模块内实体标注了 {@link DataScopeOwner @DataScopeOwner} 的表
 * （归属列随注解声明，加表只需在实体上标注，无需改本类）。
 *
 * 过滤链路（sys 领域计算全部收口在 system 模块的 DataScopeDirectory 契约，
 * 本模块不查询 sys_* 表）：业务数据归属企微成员(user_id)，
 * 可见系统账号（经 DataScopeDirectory）→ 绑定表反推可见成员 → 归属列 IN。
 * 未绑定成员的数据仅 data_scope=全部 可见。
 */
@Component
@RequiredArgsConstructor
public class ScrmDataScopeHandler implements DataScopeHandler {

    private final DataScopeScopeResolver dataScopeResolver;
    private final WxUserBindingMapper wxUserBindingMapper;

    @Override
    public boolean supports(String tableName) {
        return resolveOwnerColumn(tableName) != null;
    }

    @Override
    public Expression buildCondition(Table table) {
        Long userId = DataScopeContextHolder.getUserId();
        if (userId == null) {
            // 无登录上下文（未预期场景）→ 拒绝
            return deny();
        }

        // system 领域：角色 data_scope → 可见系统账号集合
        DataScopeView view = dataScopeResolver.resolveScope(userId);
        if (view.getMode() == DataScopeView.Mode.ALL) {
            return null;
        }
        if (view.getMode() != DataScopeView.Mode.USERS) {
            return deny();
        }

        // scrm 领域：可见账号 → 绑定的企微成员集合（跨企业）
        List<String> visibleUserids = wxUserBindingMapper.selectList(
                        Wrappers.<WxUserBinding>lambdaQuery()
                                .in(WxUserBinding::getSysUserId, view.getUserIds()))
                .stream().map(WxUserBinding::getWxUserid).distinct().collect(Collectors.toList());
        if (visibleUserids.isEmpty()) {
            return deny();
        }

        // 归属列 IN (可见成员集合)；列按 SQL 中的别名限定（无别名则裸列名）
        String ownerColumn = resolveOwnerColumn(table.getName());
        String alias = table.getAlias() == null ? null : table.getAlias().getName();
        String column = alias == null ? ownerColumn : alias + '.' + ownerColumn;
        InExpression in = new InExpression();
        in.setLeftExpression(new Column(column));
        in.setRightExpression(new ParenthesedExpressionList(
                visibleUserids.stream().map(LongValue::new).toList()));
        return in;
    }

    /** 从 MP TableInfo 元数据解析实体的 @DataScopeOwner 归属列（大小写不敏感；无注解返回 null） */
    private String resolveOwnerColumn(String tableName) {
        for (TableInfo tableInfo : TableInfoHelper.getTableInfos()) {
            if (tableInfo.getTableName() == null
                    || !tableInfo.getTableName().equalsIgnoreCase(tableName)) {
                continue;
            }
            Class<?> entityType = tableInfo.getEntityType();
            if (entityType == null) {
                return null;
            }
            DataScopeOwner owner = entityType.getAnnotation(DataScopeOwner.class);
            return owner == null ? null : owner.column();
        }
        return null;
    }

    /** 恒假条件 1 IN (0)：可见集合为空/上下文缺失时拒绝 */
    private Expression deny() {
        InExpression in = new InExpression();
        in.setLeftExpression(new Column("1"));
        in.setRightExpression(new ParenthesedExpressionList(new LongValue(0)));
        return in;
    }
}
