package io.etrace.consumer.component.processor;

import io.etrace.agent.Trace;
import io.etrace.common.compression.CompressType;
import io.etrace.common.message.trace.CallStackV1;
import io.etrace.common.message.trace.MessageItem;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.Processor;
import io.etrace.common.pipeline.impl.DefaultAsyncTask;
import io.etrace.common.util.IPUtil;
import io.etrace.common.util.NetworkInterfaceHelper;
import io.etrace.common.util.RequestIdHelper;
import io.etrace.common.util.TimeHelper;
import io.etrace.consumer.config.ConsumerProperties;
import io.etrace.consumer.model.MessageBlock;
import io.etrace.consumer.storage.hadoop.HDFSBucket;
import io.etrace.consumer.storage.hadoop.PathBuilder;
import io.etrace.consumer.storage.hbase.IHBaseClientFactory;
import io.etrace.consumer.storage.hbase.IHBaseStorageService;
import io.etrace.consumer.storage.hbase.StackTable;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static io.etrace.consumer.metrics.MetricName.*;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HDFSProcessor extends DefaultAsyncTask implements Processor {
    public final Logger LOGGER = LoggerFactory.getLogger(HDFSProcessor.class);

    private final CompressType compressType = CompressType.snappy;

    private final short idx;
    private static final AtomicInteger atomicInteger = new AtomicInteger(0);
    private String remotePath;
    private final long ip;
    private final String prefix;
    private long currentWritingHour;
    private String currentFilePath;
    private long startPos;

    private HDFSBucket bucket;
    @Autowired
    private IHBaseClientFactory IHBaseClientFactory;
    @Autowired
    private StackTable stackSchema;

    private Timer stackTimer;
    private Counter hdfsThroughput;
    private Counter hdfsError;

    @Autowired
    private ConsumerProperties consumerProperties;
    @Autowired
    private IHBaseStorageService ihBaseStorageService;

    public HDFSProcessor(String name, Component component, Map<String, Object> params) {
        super(name, component, params);

        idx = (short)atomicInteger.getAndIncrement();
        String localIp = NetworkInterfaceHelper.INSTANCE.getLocalHostAddress();
        this.ip = IPUtil.ipToLong(localIp);
        this.prefix = idx + "-" + localIp;
    }

    @Override
    public void init(Object... param) {
        super.init(param);
    }

    @Override
    public void startup() {
        this.remotePath = consumerProperties.getHdfs().getPath();

        hdfsError = Metrics.counter(HDFS_ERROR);
        hdfsThroughput = Metrics.counter(HDFS_THROUGHPUT);
        stackTimer = Metrics.timer(TASK_DURATION, Tags.of("type", "hdfs-to-hbase"));

        super.startup();
    }

    @Override
    public void processEvent(Object key, Object obj) throws Exception {
        if (obj instanceof MessageBlock) {
            MessageBlock messageBlock = (MessageBlock)obj;
            long lastPos = -1;
            try {
                long startTime = System.currentTimeMillis();
                resetCurrentWritingHourIfNeeded(startTime);

                if (null != messageBlock.getItems()) {
                    // first, write file storage
                    lastPos = retryWriteHdfs(messageBlock.getData());
                    if (lastPos < 0) {
                        LOGGER.error("fail to write data to hdfs storage, lastPos [{}]", lastPos);
                        hdfsError.increment();
                        return;
                    }
                    // second, build hBase index
                    for (MessageItem item : messageBlock.getItems()) {
                        try {
                            CallStackV1 callStack = item.getCallStack();
                            long timestamp = RequestIdHelper.getTimestamp(item.getRequestId());
                            writeToHBase(timestamp, item.getRequestId(), callStack, item.getOffset());
                        } catch (Exception e) {
                            LOGGER.error("build hbase put error:", e);
                        }
                    }
                    hdfsThroughput.increment(messageBlock.getData().length);
                }
            } catch (Exception e) {
                LOGGER.error("callStack write to hdfs error:", e);
                Trace.logError("callStack write to hdfs error", e);
                throw e;
            } finally {
                if (lastPos > 0) {
                    startPos = lastPos;
                }
            }
        }
    }

    public void writeToHBase(long timestamp, String requestId, CallStackV1 callStack, int messageOffset) {
        short shard = IHBaseClientFactory.getShardIdByLogicalTableName(stackSchema.getLogicalTableName(), timestamp,
            RequestIdHelper.getRequestId(requestId).hashCode());
        // rowKey
        byte[] qualifierValue = stackSchema.buildQualifierValue(callStack, messageOffset, startPos, currentWritingHour,
            ip, idx);
        Put put = ihBaseStorageService.buildHbasePut(timestamp, requestId, shard, stackSchema.getColumnFamily(),
            Bytes.toBytes(callStack.getId()), qualifierValue);

        long start = System.currentTimeMillis();
        component.dispatchAll(shard, put);
        stackTimer.record(Duration.ofMillis(System.currentTimeMillis() - start));
    }

    public long retryWriteHdfs(byte[] data) {
        for (int i = 0; i < 2; i++) {
            try {
                return write(data);
            } catch (Exception e) {
                if (!isRunning()) {
                    return -1;
                }
                LOGGER.error("Error when write block to hdfs error for bucket[{}] .", currentFilePath, e);
            }
        }
        return -1;
    }

    public long write(byte[] data) throws IOException {
        return bucket.writeBlock(data);
    }

    private void resetCurrentWritingHourIfNeeded(long startTime) throws IOException {
        long currentHour = TimeHelper.getHour(startTime);
        if (currentHour != currentWritingHour) {
            //flush old bucket, then exchange new bucket
            String filePath = PathBuilder.buildMessagePath(prefix, currentHour);

            if (null != bucket) {
                resetBucket();
            }
            if (null == bucket) {
                //create new HDFS or reload old HDFS bucket
                bucket = new HDFSBucket(compressType.code(), remotePath, filePath);
                startPos = bucket.getLastBlockOffset();
            }
            currentFilePath = filePath;
            currentWritingHour = currentHour;
        }
    }

    private void resetBucket() {
        try {
            //close old bucket, because it no data to write
            bucket.close();
            bucket = null;
            LOGGER.info("close bucket {} done.", currentFilePath);
        } catch (Throwable e) {
            LOGGER.error("close bucket {} error.", currentFilePath, e);
        }
    }

    @Override
    public void stop() {
        try {
            super.stop();
            if (null != bucket) {
                bucket.close();
            }
        } catch (Exception e) {
            LOGGER.error("hdfs close error:", e);
        }
    }

}
