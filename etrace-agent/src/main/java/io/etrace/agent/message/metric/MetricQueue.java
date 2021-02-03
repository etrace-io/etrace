/*
 * Copyright 2019 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.agent.message.metric;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.EventTranslatorTwoArg;
import com.lmax.disruptor.TimeoutException;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.agent.io.MessageSender;
import io.etrace.agent.io.TcpMessageSender;
import io.etrace.agent.message.QueueContext;
import io.etrace.agent.message.event.MatricPackageEvent;
import io.etrace.agent.message.event.MetricEvent;
import io.etrace.agent.stat.MetricStats;
import io.etrace.common.histogram.BucketFunction;
import io.etrace.common.histogram.DistAlgorithmBucket;
import io.etrace.common.histogram.DistributionType;
import io.etrace.common.message.agentconfig.ConfigManger;
import io.etrace.common.message.metric.MetricInTraceApi;
import io.etrace.common.message.metric.field.MetricKey;
import io.etrace.common.message.trace.codec.JSONCodecV1;
import io.etrace.common.util.ThreadUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.etrace.agent.message.callstack.CallstackQueue.PULL_INTERVAL_IN_MILLISECOND;

public class MetricQueue {
    private final static String SPLIT_STR = "##";
    protected QueueContext<MetricEvent> context;
    protected QueueContext<MatricPackageEvent> packageContext;
    @Inject
    protected ConfigManger configManger;
    @Inject
    protected MetricStats stats;
    private MetricProducer metricProducer;
    private PackageProducer packageProducer;
    private BucketFunction bucketFunction;
    private ScheduledExecutorService executorService;
    @Inject
    @Named(TcpMessageSender.METRIC_TCP_MESSAGE_SENDER)
    private MessageSender messageSender;

    public MetricQueue() {
        context = new QueueContext<>();
        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 1024 * 4;
        metricProducer = new MetricQueue.MetricProducer();
        // The factory for the event
        MetricEvent.MetricEventFactory factory = new MetricEvent.MetricEventFactory();
        EventConsumer consumer = new EventConsumer();
        context.build("Metric-Producer", bufferSize, consumer, factory);

        packageContext = new QueueContext<>();
        // Specify the size of the ring buffer, must be power of 2.
        int packageBufferSize = 16;
        packageProducer = new MetricQueue.PackageProducer();
        // The factory for the event
        MatricPackageEvent.PackageEventFactory packageFactory = new MatricPackageEvent.PackageEventFactory();
        packageContext.build("Metric-Package-Producer", packageBufferSize, new MetricQueue.PackageConsumer(),
            packageFactory);

        executorService = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("MetricQueue-Timer-%d").build());

        executorService.scheduleAtFixedRate(() -> {
            if (context.getRingBuffer() != null) {
                context.getRingBuffer().tryPublishEvent(metricProducer, null);//heartbeat
            }
            //do something
        }, 0, PULL_INTERVAL_IN_MILLISECOND, TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void produce(MetricInTraceApi metricInTraceApi) {
        if (!context.isActive()) {
            return;
        }
        stats.incTotalCount();
        boolean success = context.getRingBuffer().tryPublishEvent(metricProducer, metricInTraceApi);
        if (!success) {
            int tryAgainCount = 0;
            while (!success && tryAgainCount < 2) {
                tryAgainCount++;
                success = context.getRingBuffer().tryPublishEvent(metricProducer, metricInTraceApi);
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
        executorService.shutdown();
    }

    public int getQueueSize() {
        return context.getQueueSize();
    }

    public int getPackageQueueSize() {
        return packageContext.getQueueSize();
    }

    class MetricProducer implements EventTranslatorOneArg<MetricEvent, MetricInTraceApi> {
        @Override
        public void translateTo(MetricEvent event, long sequence, MetricInTraceApi metricInTraceApi) {
            event.reset(metricInTraceApi);
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
                MetricInTraceApi metricInTraceApi = event.getMetric();
                if (metricInTraceApi != null) {
                    String name = metricInTraceApi.getName();
                    Map<MetricKey, PackageMetric> oldMetrics = metrics.get(name);
                    if (oldMetrics == null) {
                        if (metrics.size() >= configManger.getMetricConfig().getMaxMetric()) {
                            return;
                        }
                        oldMetrics = new HashMap<>();
                        metrics.put(name, oldMetrics);
                    }
                    PackageMetric oldMetric = oldMetrics.get(metricInTraceApi.getKey());
                    if (oldMetric == null) {
                        if (oldMetrics.size() > maxType) {
                            return;
                        }
                        PackageMetric packageMetric = new PackageMetric(configManger, this, metricInTraceApi);
                        packageMetric.merge(metricInTraceApi);
                        oldMetrics.put(metricInTraceApi.getKey(), packageMetric);
                    } else {
                        oldMetric.merge(metricInTraceApi);
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
        EventTranslatorTwoArg<MatricPackageEvent, Map<String, Map<MetricKey, PackageMetric>>, Integer> {
        @Override
        public void translateTo(MatricPackageEvent event, long sequence,
                                Map<String, Map<MetricKey, PackageMetric>> metrics,
                                Integer sendCount) {
            event.reset(metrics, sendCount == null ? 0 : sendCount);
        }
    }

    class PackageConsumer implements EventHandler<MatricPackageEvent> {
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
        public void onEvent(MatricPackageEvent event, long sequence, boolean endOfBatch) throws Exception {
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
                    List<PackageMetric> packageMetrics = sendMetrics.computeIfAbsent(key, k -> new ArrayList<>());
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

        protected void send(byte[] data, int sendCount, String key) {
            messageSender.send(data, sendCount);
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

        /*
        这里的逻辑难以拆分。因为原始设计，为了精准控制发送数据大小，在遍历中处理是否超限。
         */
        private int write(PackageMetric packageMetric, int sendCount) throws IOException {
            generator.writeStartArray();
            generator.writeString(JSONCodecV1.METRIC_PREFIX_V1);
            generator.writeString(AgentConfiguration.getTenant());
            generator.writeString(AgentConfiguration.getAppId());
            generator.writeString(context.getHostIp());
            generator.writeString(context.getHostName());

            generator.writeObject(context.getExtraProperties());

            generator.writeStartArray();
            if (packageMetric.defaultMetric != null) {
                generator.writeStartArray();
                packageMetric.defaultMetric.write(generator);
                generator.writeEndArray();
                packageMetric.defaultMetric = null;
                sendCount++;
            }
            if (packageMetric.metrics != null && packageMetric.metrics.size() > 0) {
                Iterator<MetricInTraceApi<?>> metricIterator = packageMetric.metrics.values().iterator();
                while (metricIterator.hasNext()) {
                    MetricInTraceApi<?> metricInTraceApi = metricIterator.next();
                    if (metricInTraceApi == null) {
                        metricIterator.remove();
                        continue;
                    }
                    generator.writeStartArray();
                    metricInTraceApi.write(generator);
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

    }
}
