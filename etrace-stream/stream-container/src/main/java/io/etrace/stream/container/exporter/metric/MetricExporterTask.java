package io.etrace.stream.container.exporter.metric;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import io.etrace.common.message.metric.Metric;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.Exporter;
import io.etrace.common.pipeline.Resource;
import io.etrace.common.pipeline.impl.DefaultSyncTask;
import io.etrace.stream.container.service.ChannelManager;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
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

    private final Map<String, Counter> sendCounter;
    private String resourceId;
    @Autowired
    private ChannelManager channelManager;
    private Resource resource;

    public MetricExporterTask(String name, Component component, Map<String, Object> params) {
        super(name, component, params);

        this.resourceId = params.get("resourceId").toString();
        sendCounter = new ConcurrentHashMap<>();
    }

    @Override
    public void init(Object... param) {
        List<Resource> resources = (List<Resource>)param[1];
        resource = resources.stream().filter((Predicate<Resource>)r -> r.getName().equals(resourceId)).findFirst()
            .get();
    }

    @Override
    public void startup() {
        super.startup();
    }

    @Override
    public void processEvent(Object key, Object event) throws Exception {
        //todo: 不知为何 有个 startup 没有被call
        // 还有这个问题吗？
        if (channelManager == null) {
            startup();
        }

        if (event instanceof Collection) {
            Collection<Metric> collection = (Collection<Metric>)event;
            Map<String, List<Metric>> metricGroups = collection.stream().filter(
                m -> !Strings.isNullOrEmpty(m.getSource())).collect(Collectors.groupingBy(Metric::getSource));
            for (Map.Entry<String, List<Metric>> entry : metricGroups.entrySet()) {

                if (channelManager == null) {
                    System.out.println(resourceId + "\t" + entry + "\t" + channelManager);
                }

                channelManager.writeData(resourceId, entry.getKey(), entry.getValue());
                getSendCounter(entry.getKey()).increment(entry.getValue().size());
            }
        } else if (event instanceof Metric) {
            throw new RuntimeException("error to be here when input the Metric !!!");
        }
    }

    private Counter getSendCounter(String database) {
        Counter counter = sendCounter.get(database);
        if (counter == null) {
            counter = Counter.builder(STREAM_PRODUCER_SEND)
                .tag("pipeline", component.getPipeline())
                .tag("database", database)
                .register(Metrics.globalRegistry);
            sendCounter.put(database, counter);
        }
        return counter;
    }
}
