package io.etrace.consumer.component.processor;

import io.etrace.common.compression.CompressType;
import io.etrace.common.message.trace.CallStackV1;
import io.etrace.common.message.trace.MessageItem;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.impl.DefaultAsyncTask;
import io.etrace.common.util.IPUtil;
import io.etrace.common.util.NetworkInterfaceHelper;
import io.etrace.common.util.RequestIdHelper;
import io.etrace.common.util.TimeHelper;
import io.etrace.consumer.config.ConsumerProperties;
import io.etrace.consumer.model.MessageBlock;
import io.etrace.consumer.storage.hadoop.HDFSBucket;
import io.etrace.consumer.storage.hadoop.PathBuilder;
import io.etrace.consumer.storage.hbase.HBaseClientFactory;
import io.etrace.consumer.storage.hbase.PutBuilder;
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

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import static io.etrace.consumer.metrics.MetricName.*;

@org.springframework.stereotype.Component

public class HDFSProcessor extends DefaultAsyncTask {
    public final Logger LOGGER = LoggerFactory.getLogger(HDFSProcessor.class);

    private final CompressType compressType = CompressType.snappy;

    private short idx;
    private String remotePath;
    private long ip;
    private String prefix;
    private long currentWritingHour;
    private String currentFilePath;
    private long startPos;

    private HDFSBucket bucket;
    @Autowired
    private HBaseClientFactory hBaseClientFactory;
    @Autowired
    private StackTable stackSchema;

    private Timer stackTimer;
    private Counter hdfsThroughput;
    private Counter hdfsError;

    @Autowired
    private ConsumerProperties consumerProperties;

    public HDFSProcessor(String name, Component component, Map<String, Object> params) {
        super(name, component, params);

        idx = Short.parseShort(name.split("-")[2]);
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
            } finally {
                if (lastPos > 0) {
                    startPos = lastPos;
                }
            }
        }
    }

    public void writeToHBase(long timestamp, String requestId, CallStackV1 callStack, int messageOffset) {
        // rowKey
        short shard = hBaseClientFactory.getShardId(timestamp, RequestIdHelper.getRequestId(requestId).hashCode());
        Put put = PutBuilder.createPut(PutBuilder.createRowKey(shard, requestId), timestamp);

        byte[] qualifierValue = stackSchema.buildQualifierValue(callStack, messageOffset, startPos, currentWritingHour,
            ip, idx);
        put.addColumn(stackSchema.getCf(), Bytes.toBytes(callStack.getId()), qualifierValue);

        long start = System.currentTimeMillis();
        component.dispatch(shard, put);
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
