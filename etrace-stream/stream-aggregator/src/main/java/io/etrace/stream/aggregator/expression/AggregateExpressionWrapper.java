package io.etrace.stream.aggregator.expression;

import com.espertech.esper.client.soda.Expression;

import java.util.Collections;
import java.util.Set;

public class AggregateExpressionWrapper<E extends Expression> extends ExpressionWrapperBase<E> {

    public AggregateExpressionWrapper(Expression expression, String asName) {
        super(expression, asName);
    }

    @Override
    public Set<String> items() {
        return Collections.emptySet();
    }
}
