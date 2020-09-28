package io.etrace.stream.aggregator.expression;

import com.espertech.esper.client.soda.Expression;
import com.espertech.esper.client.soda.PropertyValueExpression;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class PropertyExpressionWrapper<E extends Expression> extends ExpressionWrapperBase<E> {

    private PropertyValueExpression propertyValueExpression;

    public PropertyExpressionWrapper(PropertyValueExpression expression, String asName) {
        super(expression, asName);
        propertyValueExpression = expression;
    }

    @Override
    public Set<String> items() {
        String item = EPClausUtils.getItem(propertyValueExpression);
        return newHashSet(trim(item));
    }
}
