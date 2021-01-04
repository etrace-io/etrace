package io.etrace.stream.aggregator;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.event.map.MapEventBean;
import com.google.common.base.Strings;
import io.etrace.common.message.metric.Metric;
import io.etrace.common.message.metric.field.MetricKey;
import io.etrace.common.pipeline.Component;
import io.etrace.stream.aggregator.annotation.MetricProcessor;
import io.etrace.stream.aggregator.function.GroupByObjects;
import io.etrace.stream.core.util.MetricUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.google.common.collect.Maps.newHashMap;
import static io.etrace.common.constant.InternalMetricName.EP_ENGINE_UPDATE_EVENT;

public class EPAggregatorEventListener implements StatementAwareUpdateListener {
    private final static Logger LOGGER = LoggerFactory.getLogger(EPAggregatorEventListener.class);
    private final Component component;
    private final Map<String, Counter> counters;
    private String defaultSource;

    public EPAggregatorEventListener(Component component, Map<String, Object> params) {
        this.component = component;

        this.defaultSource = String.valueOf(params.get("source"));
        counters = new HashMap<>();
    }

    protected static MetricKey metricKey(Metric metric) {
        return GroupByObjects.GroupByHashKey(metric.getMetricName(), metric.getMetricType(),
            metric.getSource(), metric.getTags(), metric.getTimestamp());
    }

    @SuppressWarnings("unchecked")
    public void update(EventBean[] newEvents, EventBean[] oldEvents,
                       EPStatement statement, EPServiceProvider epServiceProvider) {
        try {
            if (newEvents != null) {
                Counter counter = getCounter(statement.getName());
                if (counter != null) {
                    counter.increment(newEvents.length);
                }
                Object userObject = statement.getUserObject();
                Map<String, Object> metaData = null;
                if (userObject != null) {
                    metaData = (Map<String, Object>)userObject;
                }
                if (metaData == null) {
                    return;
                }
                // todo metricKey冲突
                Map<MetricKey, Metric> metricMap = newHashMap();
                for (EventBean newEvent : newEvents) {
                    if (!(newEvent instanceof MapEventBean)) {
                        continue;
                    }

                    MapEventBean mapEventBean = (MapEventBean)newEvent;

                    List<MetricProcessor> processors =
                        (List<MetricProcessor>)metaData.get(EPEngine.EPL_METRIC_PROCESSORS);
                    if (processors != null && processors.size() > 0) {
                        for (MetricProcessor metricProcessor : processors) {
                            Metric metric = metricProcessor.process(mapEventBean);
                            if (metric != null) {
                                if (Strings.isNullOrEmpty(metric.getSource())) {
                                    metric.setSource(defaultSource);
                                }
                                MetricKey key = metricKey(metric);
                                Metric exists = metricMap.get(key);
                                if (exists == null) {
                                    metricMap.put(key, metric);
                                } else {
                                    MetricUtil.merge(exists, metric);
                                }
                            }
                        }
                    }
                }
                Collection<Metric> list = metricMap.values();
                // todo:  key should be what?
                component.dispatchAll("", list);
            }
        } catch (Exception e) {
            String msg = "process aggregator event listener error";
            LOGGER.error(msg, e);
            //            component.setMsg(msg);
            //            component.setThrowable(e);
        }
    }

    private Counter getCounter(String policy) {
        Counter counter = counters.get(policy);
        if (counter == null) {
            counter = Counter.builder(EP_ENGINE_UPDATE_EVENT)
                .tag("pipeline", component.getPipeline())
                .tag("name", component.getName())
                .tag("policy", policy)
                .register(Metrics.globalRegistry);

            counters.put(policy, counter);
        }
        return counter;
    }
}
