package io.etrace.stream.container.exporter.metric;

import com.google.common.base.Strings;
import io.etrace.common.message.metric.Metric;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.Exporter;
import io.etrace.common.pipeline.impl.DefaultSyncTask;
import io.etrace.stream.container.service.ChannelManager;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.etrace.common.constant.InternalMetricName.STREAM_PRODUCER_SEND;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MetricExporterTask extends DefaultSyncTask implements Exporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricExporterTask.class);

    public static final String FORCED_DATABASE = "forcedDatabase";

    private final Map<String, Counter> sendCounter = new ConcurrentHashMap<>();
    private String resourceId;
    @Autowired
    private ChannelManager channelManager;

    private String forcedDatabase = null;

    public MetricExporterTask(String name, Component component, Map<String, Object> params) {
        super(name, component, params);
        this.resourceId = String.valueOf(params.get("resourceId"));

        if (params.get(FORCED_DATABASE) != null) {
            LOGGER.warn("MetricExporterTask will write ALL data to database [{}] instead of the origin datasource in "
                + "data properties!", forcedDatabase);
            forcedDatabase = String.valueOf(params.get(FORCED_DATABASE));
        }
    }

    @Override
    public void startup() {
        super.startup();
    }

    @Override
    public void processEvent(Object key, Object event) throws Exception {
        if (event instanceof Collection) {
            Collection<Metric> collection = (Collection<Metric>)event;
            Map<String, List<Metric>> metricGroups = collection.stream().filter(
                m -> !Strings.isNullOrEmpty(m.getSource()) && !m.getMetricName().equals("stream.esper.check.flush.event")
            ).collect(Collectors.groupingBy(Metric::getSource));
            for (Map.Entry<String, List<Metric>> entry : metricGroups.entrySet()) {
                String database = forcedDatabase != null ? forcedDatabase : entry.getKey();
                channelManager.writeData(resourceId, database, entry.getValue());
                getSendCounter(database, entry.getKey()).increment(entry.getValue().size());
            }
        } else if (event instanceof Metric) {
            throw new RuntimeException("Should be List<Metric> in MetricExporterTask");
        }
    }

    private Counter getSendCounter(String database, String originDatabase) {
        Counter counter = sendCounter.get(database);
        if (counter == null) {
            counter = Counter.builder(STREAM_PRODUCER_SEND)
                .tag("pipeline", component.getPipeline())
                .tag("database", database)
                .tag("originDatabase", originDatabase)
                .register(Metrics.globalRegistry);
            sendCounter.put(database, counter);
        }
        return counter;
    }
}
