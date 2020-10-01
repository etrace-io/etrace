package io.etrace.stream.aggregator.plugin;

import com.espertech.esper.epl.agg.aggregator.AggregationMethod;

public class GetValueAggregator implements AggregationMethod {
    private Object item;

    @Override
    public void enter(Object o) {
        item = o;
    }

    @Override
    public void leave(Object o) {

    }

    @Override
    public Object getValue() {
        return item;
    }

    @Override
    public void clear() {
        item = null;
    }
}
