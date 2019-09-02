package io.etrace.agent.message.metric;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.EventTranslatorTwoArg;
import com.lmax.disruptor.TimeoutException;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.agent.message.ProducerContext;
import io.etrace.agent.message.event.MetricEvent;
import io.etrace.agent.message.event.PackageEvent;
import io.etrace.agent.message.io.MessageSender;
import io.etrace.agent.stat.MetricStats;
import io.etrace.common.histogram.BucketFunction;
import io.etrace.common.histogram.DistAlgorithmBucket;
import io.etrace.common.histogram.DistributionType;
import io.etrace.common.message.ConfigManger;
import io.etrace.common.modal.metric.Metric;
import io.etrace.common.modal.metric.MetricKey;
import io.etrace.common.util.ThreadUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MetricQueue {
    private final static String SPLIT_STR = "##";
    protected ProducerContext<MetricEvent> context;
    protected ProducerContext<PackageEvent> packageContext;
    @Inject
    protected ConfigManger configManger;
    @Inject
    protected MetricStats stats;
    private MetricProducer metricProducer;
    private PackageProducer packageProducer;
    private BucketFunction bucketFunction;
    private Timer timer;
    @Inject
    @Named("metricTCPMessageSender")
    private MessageSender messageSender;

    public MetricQueue() {
        context = new ProducerContext<>();
        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024 * 4;
        metricProducer = new MetricQueue.MetricProducer();
        // The factory for the event
        MetricEvent.MetricEventFactory factory = new MetricEvent.MetricEventFactory();
        EventConsumer comsumer = new EventConsumer();
        context.build("Metric-Producer", bufferSize, comsumer, factory);

        packageContext = new ProducerContext<>();
        // Specify the size of the ring buffer, must be power of 2.
        int packageBufferSize = 16;
        packageProducer = new MetricQueue.PackageProducer();
        // The factory for the event
        PackageEvent.PackageEventFactory packageFactory = new PackageEvent.PackageEventFactory();
        packageContext.build("Metric-Package-Producer", packageBufferSize, new MetricQueue.PackageConsumer(),
            packageFactory);

        timer = new Timer("MetricQueue-Timer-" + System.currentTimeMillis());
        int pullIntervalSeconds = 2;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (context.getRingBuffer() != null) {
                    context.getRingBuffer().tryPublishEvent(metricProducer, null);//heartbeat
                }
            }
        }, 0, pullIntervalSeconds * 1000);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    public void produce(Metric metric) {
        if (!context.isActive()) {
            return;
        }
        stats.incTotalCount();
        boolean success = context.getRingBuffer().tryPublishEvent(metricProducer, metric);
        if (!success) {
            int tryAgainCount = 0;
            while (!success && tryAgainCount < 2) {
                tryAgainCount++;
                success = context.getRingBuffer().tryPublishEvent(metricProducer, metric);
            }
        }
        if (!success) {
            stats.incLoss();
            ThreadUtil.sleep(0);
        }
    }

    public void shutdown() {
        try {
            context.setActive(false);
            context.getRingBuffer().tryPublishEvent(metricProducer, null);//heartbeat
            context.getDisruptor().shutdown(2, TimeUnit.SECONDS);
        } catch (TimeoutException ignore) {
        }
        timer.cancel();
    }

    public int getQueueSize() {
        return context.getQueueSize();
    }

    public int getPackageQueueSize() {
        return packageContext.getQueueSize();
    }

    class MetricProducer implements EventTranslatorOneArg<MetricEvent, Metric> {
        @Override
        public void translateTo(MetricEvent event, long sequence, Metric metric) {
            event.reset(metric);
        }
    }

    protected class EventConsumer implements EventHandler<MetricEvent> {
        protected int sendCount;
        protected int mergeCount;
        protected long start = System.currentTimeMillis();
        //name             type + time + topic    PackageMetric{tags or not tags}
        protected Map<String, Map<MetricKey, PackageMetric>> metrics = new HashMap<>();
        private int maxType = 3 * 5;

        public EventConsumer() {
        }

        @Override
        public void onEvent(MetricEvent event, long sequence, boolean endOfBatch) throws Exception {
            try {
                Metric metric = event.getMetric();
                if (metric != null) {
                    String name = metric.getName();
                    Map<MetricKey, PackageMetric> oldMetrics = metrics.get(name);
                    if (oldMetrics == null) {
                        if (metrics.size() >= configManger.getMetricConfig().getMaxMetric()) {
                            return;
                        }
                        oldMetrics = new HashMap<>();
                        metrics.put(name, oldMetrics);
                    }
                    PackageMetric oldMetric = oldMetrics.get(metric.getKey());
                    if (oldMetric == null) {
                        if (oldMetrics.size() > maxType) {
                            return;
                        }
                        PackageMetric packageMetric = new PackageMetric(configManger, this, metric);
                        packageMetric.merge(metric);
                        oldMetrics.put(metric.getKey(), packageMetric);
                    } else {
                        oldMetric.merge(metric);
                    }
                }
                long endTime = System.currentTimeMillis();
                if (endTime - start >= 2000 || sendCount > 150000 || !context.isActive()) {
                    if (!tryPublishEvent()) {
                        stats.incPackageLoss(sendCount + mergeCount);
                    } else {
                        stats.incMerge(mergeCount);
                    }
                    metrics = new HashMap<>();
                    sendCount = 0;
                    mergeCount = 0;
                    start = System.currentTimeMillis();
                }
            } finally {
                event.clear();
            }
        }

        protected boolean tryPublishEvent() {
            return packageContext.getRingBuffer().tryPublishEvent(packageProducer, metrics, sendCount);
        }

        public BucketFunction getBucketFunction() {
            if (bucketFunction == null) {
                bucketFunction = DistAlgorithmBucket.buildBucketFunction(DistributionType.Percentile, 0);
            }
            return bucketFunction;
        }
    }

    class PackageProducer implements
        EventTranslatorTwoArg<PackageEvent, Map<String, Map<MetricKey, PackageMetric>>, Integer> {
        @Override
        public void translateTo(PackageEvent event, long sequence, Map<String, Map<MetricKey, PackageMetric>> metrics,
                                Integer sendCount) {
            event.reset(metrics, sendCount == null ? 0 : sendCount);
        }
    }

    class PackageConsumer implements EventHandler<PackageEvent> {
        private int maxSize = 1024 * 1024 * 1;
        private JsonFactory jsonFactory;
        private ByteArrayOutputStream baos;
        private JsonGenerator generator;

        public PackageConsumer() {
            jsonFactory = new JsonFactory();
            baos = new ByteArrayOutputStream();
            try {
                generator = jsonFactory.createJsonGenerator(baos, JsonEncoding.UTF8);
            } catch (IOException ignore) {
            }
        }

        @Override
        public void onEvent(PackageEvent event, long sequence, boolean endOfBatch) throws Exception {
            if (event.getMetrics() == null || event.getSendCount() <= 0) {
                event.clear();
                return;
            }

            Map<String, Map<MetricKey, PackageMetric>> metrics = event.getMetrics();
            Map<String, List<PackageMetric>> sendMetrics = new HashMap<>();
            for (Map.Entry<String, Map<MetricKey, PackageMetric>> stringMapEntry : metrics.entrySet()) {
                String name = stringMapEntry.getKey();
                Map<MetricKey, PackageMetric> packageMetricMap = stringMapEntry.getValue();
                if (packageMetricMap == null || packageMetricMap.isEmpty()) {
                    continue;
                }
                for (PackageMetric packageMetric : stringMapEntry.getValue().values()) {
                    if (packageMetric == null || packageMetric.isEmpty()) {
                        continue;
                    }
                    String key = packageMetric.getTopic() + SPLIT_STR + name;
                    List<PackageMetric> packageMetrics = sendMetrics.get(key);
                    if (packageMetrics == null) {
                        packageMetrics = new ArrayList<>();
                        sendMetrics.put(key, packageMetrics);
                    }
                    packageMetrics.add(packageMetric);
                }
            }
            if (!sendMetrics.isEmpty()) {
                for (Map.Entry<String, List<PackageMetric>> packageMetricEntry : sendMetrics.entrySet()) {
                    try {
                        send(packageMetricEntry.getKey(), packageMetricEntry.getValue());
                    } catch (Exception e) {
                        generator.flush();
                        generator.close();
                        baos.reset();
                        try {
                            generator = jsonFactory.createJsonGenerator(baos, JsonEncoding.UTF8);
                        } catch (IOException ignore) {
                        }
                    }
                }
            }
            event.clear();
        }

        private void flush(int sendCount, String key) throws IOException {
            generator.flush();
            if (baos != null && baos.size() > 0) {
                try {
                    if (sendCount > 0) {
                        stats.incMergeAfterTotal(sendCount);
                        send(baos.toByteArray(), sendCount, key);
                    }
                } finally {
                    baos.reset();
                }
            }
        }

        private void send(String key, List<PackageMetric> metrics) throws IOException {
            int sendCount = 0;
            generator.writeStartArray();
            Iterator<PackageMetric> packageMetrics = metrics.iterator();
            while (packageMetrics.hasNext()) {
                PackageMetric packageMetric = packageMetrics.next();
                while (!packageMetric.isEmpty()) {
                    sendCount = write(packageMetric, sendCount);
                    if (sendCount > configManger.getMetricConfig().getMaxPackageCount() || baos.size() >= maxSize) {
                        generator.writeEndArray();
                        flush(sendCount, key);
                        sendCount = 0;
                        generator.writeStartArray();
                    }
                }
                packageMetrics.remove();
            }
            generator.writeEndArray();
            flush(sendCount, key);
        }

        private int write(PackageMetric packageMetric, int sendCount) throws IOException {
            generator.writeStartArray();
            generator.writeString(packageMetric.getTopic());
            generator.writeString(AgentConfiguration.getServiceName());
            generator.writeString(context.getHostIp());
            generator.writeString(context.getHostName());

            generator.writeString(context.getCluster());
            generator.writeString(context.getEzone());
            generator.writeString(context.getIdc());

            generator.writeString(context.getMesosTaskId());
            generator.writeString(context.getEleapposLabel());
            generator.writeString(context.getEleapposSlaveFqdn());
            generator.writeStartArray();
            if (packageMetric.defaultMetric != null) {
                generator.writeStartArray();
                packageMetric.defaultMetric.write(generator);
                generator.writeEndArray();
                packageMetric.defaultMetric = null;
                sendCount++;
            }
            if (packageMetric.metrics != null && packageMetric.metrics.size() > 0) {
                Iterator<Metric> metricIterator = packageMetric.metrics.values().iterator();
                while (metricIterator.hasNext()) {
                    Metric metric = metricIterator.next();
                    if (metric == null) {
                        metricIterator.remove();
                        continue;
                    }
                    generator.writeStartArray();
                    metric.write(generator);
                    generator.writeEndArray();
                    metricIterator.remove();
                    sendCount++;
                    if (sendCount > configManger.getMetricConfig().getMaxPackageCount() || baos.size() >= maxSize) {
                        break;
                    }
                }
            }
            generator.writeEndArray();
            generator.writeEndArray();
            return sendCount;
        }

        protected void send(byte[] data, int sendCount, String key) {
            messageSender.send(baos.toByteArray(), sendCount, key);
        }

    }
}
