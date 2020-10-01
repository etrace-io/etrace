package io.etrace.stream.aggregator.expression;

import com.espertech.esper.client.soda.ConcatExpression;
import com.espertech.esper.client.soda.Expression;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class ConcatExpressionWrapper<E extends Expression> extends ExpressionWrapperBase<E> {

    private ConcatExpression concatExpression;

    public ConcatExpressionWrapper(ConcatExpression expression, String asName) {
        super(expression, asName);
        concatExpression = expression;
    }

    @Override
    public Set<String> items() {
        Set<String> result = newHashSet();
        for (Expression expr : concatExpression.getChildren()) {
            Set<String> items = ExpressionWrapperFactory.build(expr, null).items();
            result.addAll(items);
        }
        return result;
    }

}
