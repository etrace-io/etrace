package io.etrace.stream.aggregator.plugin;

import com.espertech.esper.client.hook.AggregationFunctionFactory;
import com.espertech.esper.epl.agg.aggregator.AggregationMethod;
import com.espertech.esper.epl.agg.service.common.AggregationValidationContext;
import io.etrace.stream.aggregator.annotation.AggregatorFunction;

@AggregatorFunction(name = "get_value")
public class GetValueAggregatorFactory implements AggregationFunctionFactory {
    @Override
    public void setFunctionName(String s) {

    }

    @Override
    public void validate(AggregationValidationContext aggregationValidationContext) {

    }

    @Override
    public AggregationMethod newAggregator() {
        return new GetValueAggregator();
    }

    @Override
    public Class getValueType() {
        return Object.class;
    }
}
