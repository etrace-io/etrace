package io.etrace.stream.aggregator.expression;

import com.espertech.esper.client.soda.DotExpressionItem;
import com.espertech.esper.client.soda.Expression;
import com.espertech.esper.client.soda.PropertyValueExpression;
import com.espertech.esper.client.soda.SingleRowMethodExpression;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class SingleRowExpressionWrapper<E extends Expression> extends ExpressionWrapperBase<E> {
    private static final String METRIC_KEY = "metric_key";

    private SingleRowMethodExpression singleRowMethodExpression;

    public SingleRowExpressionWrapper(SingleRowMethodExpression expression, String asName) {
        super(expression, asName);
        singleRowMethodExpression = expression;
    }

    /**
     * 子表达式中 存在property 返回整个表达式 trunc_sec(timestamp, 10)  -> trunc_sec(timestamp, 10) f_sum(sum(fields('count'))) ->
     * empty
     * <p>
     * metric_key特殊处理 metric_key(tag1, tag2) -> tag1, tag2
     *
     * @return
     */
    @Override
    public Set<String> items() {

        List<DotExpressionItem> dotExpressionItemList = singleRowMethodExpression.getChain();

        if (dotExpressionItemList.size() != 1) {
            throw new IllegalStateException("singleRowFunction 不支持嵌套");
        }

        DotExpressionItem dotExpressionItem = dotExpressionItemList.get(0);

        if (METRIC_KEY.equals(dotExpressionItem.getName())) {
            return metricKeyParameters(dotExpressionItem);
        }

        Set<String> result = newHashSet();
        boolean hasProperty = false;
        for (Expression expr : dotExpressionItem.getParameters()) {
            if (ExpressionWrapperFactory.isProperty(expr)) {
                hasProperty = true;
                break;
            }
        }

        if (hasProperty) {
            result.add(EPClausUtils.getItem(singleRowMethodExpression));
        }
        return result;
    }

    private Set<String> metricKeyParameters(DotExpressionItem dotExpressionItem) {
        Set<String> result = newHashSet();
        for (Expression expr : dotExpressionItem.getParameters()) {
            if (!ExpressionWrapperFactory.isProperty(expr)) {
                throw new IllegalStateException("parameters in metric_key() should all be property");
            }
            PropertyValueExpression propertyValueExpression = (PropertyValueExpression)expr;
            result.add(propertyValueExpression.getPropertyName());
        }
        return result;
    }
}
