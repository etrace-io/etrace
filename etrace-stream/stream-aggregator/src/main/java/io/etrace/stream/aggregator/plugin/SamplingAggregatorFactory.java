package io.etrace.stream.aggregator.plugin;

import com.espertech.esper.client.hook.AggregationFunctionFactory;
import com.espertech.esper.epl.agg.aggregator.AggregationMethod;
import com.espertech.esper.epl.agg.service.common.AggregationValidationContext;
import io.etrace.common.util.Pair;
import io.etrace.stream.aggregator.annotation.AggregatorFunction;

@AggregatorFunction(name = "sampling")
public class SamplingAggregatorFactory implements AggregationFunctionFactory {
    @Override
    public void setFunctionName(String functionName) {

    }

    @Override
    public void validate(AggregationValidationContext validationContext) {

    }

    @Override
    public AggregationMethod newAggregator() {
        return new SamplingAggregator();
    }

    @Override
    public Class getValueType() {
        return Pair.class;
    }
}
