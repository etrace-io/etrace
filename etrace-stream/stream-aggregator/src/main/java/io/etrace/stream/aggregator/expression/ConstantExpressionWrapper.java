package io.etrace.stream.aggregator.expression;

import com.espertech.esper.client.soda.ConstantExpression;
import com.espertech.esper.client.soda.Expression;

import java.util.Collections;
import java.util.Set;

public class ConstantExpressionWrapper<E extends Expression> extends ExpressionWrapperBase<E> {
    private ConstantExpression constantExpression;

    public ConstantExpressionWrapper(ConstantExpression expression, String asName) {
        super(expression, asName);
        constantExpression = expression;
    }

    @Override
    public Set<String> items() {
        return Collections.emptySet();
    }
}
