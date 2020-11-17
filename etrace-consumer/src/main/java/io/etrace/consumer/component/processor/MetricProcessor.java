package io.etrace.consumer.component.processor;

import io.etrace.common.io.BlockStoreReader;
import io.etrace.common.message.metric.Metric;
import io.etrace.common.message.metric.codec.FramedMetricMessageCodec;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.Processor;
import io.etrace.common.pipeline.impl.DefaultAsyncTask;
import io.etrace.common.util.Pair;
import io.etrace.consumer.service.HBaseBuildService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.util.List;
import java.util.Map;

import static io.etrace.consumer.metrics.MetricName.METRIC_NO_SAMPLING;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MetricProcessor extends DefaultAsyncTask implements Processor {
    public final Logger LOGGER = LoggerFactory.getLogger(MetricProcessor.class);

    private FramedMetricMessageCodec codec;
    @Autowired
    private HBaseBuildService hBaseBuildService;
    private Counter noSamplingCounter;

    public MetricProcessor(String name, Component component, Map<String, Object> params) {
        super(name, component, params);

        this.codec = new FramedMetricMessageCodec();
        noSamplingCounter = Metrics.counter(METRIC_NO_SAMPLING);
    }

    @Override
    public void startup() {
        super.startup();
    }

    @Override
    public void processEvent(Object key, Object obj) throws Exception {
        if (!(obj instanceof byte[])) {
            return;
        }
        byte[] data = (byte[])obj;
        for (byte[] messageData : BlockStoreReader.newSnappyIterator(data)) {
            try {
                List<Metric> metrics = codec.decode(messageData).getMetrics();
                for (Metric metric : metrics) {
                    try {
                        Pair<Short, Put> pair = hBaseBuildService.buildMetricIndex(metric);
                        if (null == pair) {
                            noSamplingCounter.increment();
                        } else {
                            component.dispatchAll(pair.getKey(), pair.getValue());
                        }
                    } catch (Exception e) {
                        LOGGER.error("build metric index throw a exception:", e);
                    }
                }
            } catch (Exception e) {
                //LOGGER.error("decode metric error,message is{}", StringUtils.toString(data, 0, Math.min(1024, data
                // .length)), e);
            }
        }
    }
}
