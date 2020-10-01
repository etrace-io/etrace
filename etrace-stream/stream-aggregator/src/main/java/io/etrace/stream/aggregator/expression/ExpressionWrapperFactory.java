package io.etrace.stream.aggregator.expression;

import com.espertech.esper.client.soda.*;

public class ExpressionWrapperFactory {
    private ExpressionWrapperFactory() {
    }

    public static ExpressionWrapperBase build(Expression expression, String asName) {
        if (isAggregate(expression)) {
            return new AggregateExpressionWrapper(expression, asName);
        }

        if (isConstant(expression)) {
            return new ConstantExpressionWrapper((ConstantExpression)expression, asName);
        }

        if (isSingleRow(expression)) {
            return new SingleRowExpressionWrapper((SingleRowMethodExpression)expression, asName);
        }
        if (isProperty(expression)) {
            return new PropertyExpressionWrapper((PropertyValueExpression)expression, asName);
        }

        if (isConcat(expression)) {
            return new ConcatExpressionWrapper((ConcatExpression)expression, asName);
        }

        throw new UnsupportedOperationException();
    }

    public static boolean isAggregate(Expression expression) {
        return expression instanceof SumProjectionExpression
            || expression instanceof MinProjectionExpression
            || expression instanceof MaxProjectionExpression
            || expression instanceof AvgProjectionExpression
            || expression instanceof MedianProjectionExpression
            || expression instanceof CountProjectionExpression
            || expression instanceof PlugInProjectionExpression;
    }

    public static boolean isConstant(Expression expression) {
        return expression instanceof ConstantExpression;
    }

    public static boolean isSingleRow(Expression expression) {
        return expression instanceof SingleRowMethodExpression;
    }

    public static boolean isProperty(Expression expression) {
        return expression instanceof PropertyValueExpression;
    }

    public static boolean isConcat(Expression expression) {
        return expression instanceof ConcatExpression;
    }
}
