package io.etrace.stream.aggregator.plugin;

import com.espertech.esper.client.hook.AggregationFunctionFactory;
import com.espertech.esper.epl.agg.aggregator.AggregationMethod;
import com.espertech.esper.epl.agg.service.common.AggregationValidationContext;
import io.etrace.stream.aggregator.annotation.AggregatorFunction;

import java.util.Map;

@AggregatorFunction(name = "fields_agg")
public class FieldsAggregatorFactory implements AggregationFunctionFactory {
    @Override
    public void setFunctionName(String functionName) {

    }

    @Override
    public void validate(AggregationValidationContext validationContext) {
    }

    @Override
    public AggregationMethod newAggregator() {
        return new FieldsAggregator();
    }

    @Override
    public Class getValueType() {
        return Map.class;
    }
}
