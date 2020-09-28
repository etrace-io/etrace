package io.etrace.stream.aggregator.plugin;

import com.espertech.esper.client.hook.AggregationFunctionFactory;
import com.espertech.esper.epl.agg.aggregator.AggregationMethod;
import com.espertech.esper.epl.agg.service.common.AggregationValidationContext;
import io.etrace.stream.aggregator.FlushCounter;
import io.etrace.stream.aggregator.annotation.AggregatorFunction;

/**
 * AggregatorFactory为engine通过反射生成 无法将engine中的类似计数器传进来 使用thread local记录不同engine的group数
 */
@AggregatorFunction(name = GroupCountAggregatorFactory.METHOD_NAME)
public class GroupCountAggregatorFactory implements AggregationFunctionFactory {
    public static final String METHOD_NAME = "group_count";

    @Override
    public void setFunctionName(String functionName) {

    }

    @Override
    public void validate(AggregationValidationContext validationContext) {

    }

    @Override
    public AggregationMethod newAggregator() {
        FlushCounter.increase();
        return new GroupCountAggregator();
    }

    @Override
    public Class getValueType() {
        return Long.class;
    }

}
