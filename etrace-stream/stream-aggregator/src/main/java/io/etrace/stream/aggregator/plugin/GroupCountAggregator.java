package io.etrace.stream.aggregator.plugin;

import com.espertech.esper.epl.agg.aggregator.AggregationMethod;

public class GroupCountAggregator implements AggregationMethod {
    // do nothing
    @Override
    public void enter(Object value) {

    }

    @Override
    public void leave(Object value) {

    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public void clear() {

    }
}
