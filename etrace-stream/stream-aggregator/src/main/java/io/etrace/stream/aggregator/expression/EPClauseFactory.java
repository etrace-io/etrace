package io.etrace.stream.aggregator.expression;

import com.espertech.esper.client.soda.*;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class EPClauseFactory {
    private EPClauseFactory() {
    }

    public static EPClause buildGroupByClaus(EPStatementObjectModel model) {
        GroupByClause groupByClause = model.getGroupByClause();

        List<ExpressionWrapper> wrappers = newArrayList();
        if (groupByClause == null || groupByClause.getGroupByExpressions().isEmpty()) {
            return new EPClause(wrappers);
        }

        for (GroupByClauseExpression groupByClauseExpression : groupByClause.getGroupByExpressions()) {
            if (!(groupByClauseExpression instanceof GroupByClauseExpressionSingle)) {
                throw new UnsupportedOperationException();
            }

            GroupByClauseExpressionSingle single = (GroupByClauseExpressionSingle)groupByClauseExpression;

            wrappers.add(ExpressionWrapperFactory.build(single.getExpression(), null));
        }
        return new EPClause(wrappers);
    }

    public static EPClause buildSelectClause(EPStatementObjectModel model) {
        List<ExpressionWrapper> wrappers = newArrayList();
        SelectClause selectClause = model.getSelectClause();
        if (selectClause == null || selectClause.getSelectList().isEmpty()) {
            return new EPClause(wrappers);
        }
        for (SelectClauseElement element : selectClause.getSelectList()) {
            if (!(element instanceof SelectClauseExpression)) {
                throw new UnsupportedOperationException();
            }
            SelectClauseExpression selectClauseExpression = (SelectClauseExpression)element;
            wrappers.add(ExpressionWrapperFactory
                .build(selectClauseExpression.getExpression(), selectClauseExpression.getAsName()));
        }

        return new EPClause(wrappers);
    }
}
