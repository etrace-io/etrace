package io.etrace.stream.aggregator.expression;

import com.espertech.esper.client.soda.Expression;
import io.micrometer.core.instrument.util.StringUtils;

public abstract class ExpressionWrapperBase<E extends Expression> implements ExpressionWrapper {
    private Expression expression;
    private String asName;

    public ExpressionWrapperBase(Expression expression, String asName) {
        this.expression = expression;
        this.asName = trim(asName);
    }

    @Override
    public String asName() {
        if (StringUtils.isNotEmpty(asName)) {
            return asName;
        }
        return trim(EPClausUtils.getItem(expression));
    }

    protected String trim(String item) {
        if (StringUtils.isEmpty(item)) {
            return item;
        }
        if (item.startsWith("`") && item.endsWith("`")) {
            return item.substring(1, item.length() - 1);
        } else {
            return item;
        }
    }

}