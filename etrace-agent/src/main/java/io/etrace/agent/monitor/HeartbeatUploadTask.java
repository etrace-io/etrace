package io.etrace.agent.monitor;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.inject.Inject;
import io.etrace.agent.Trace;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.agent.config.DiskFileConfiguration;
import io.etrace.agent.message.MessageProducer;
import io.etrace.agent.message.io.Client;
import io.etrace.agent.message.io.SocketClientFactory;
import io.etrace.agent.monitor.jvm.EnvironmentConfig;
import io.etrace.agent.monitor.jvm.JvmExecutor;
import io.etrace.agent.monitor.jvm.JvmThreadExecutor;
import io.etrace.agent.monitor.mbean.MBeanExecutor;
import io.etrace.agent.stat.EsightStats;
import io.etrace.agent.stat.HeartbeatStats;
import io.etrace.agent.stat.MessageStats;
import io.etrace.agent.stat.MetricStats;
import io.etrace.common.Constants;
import io.etrace.common.message.MessageManager;
import io.etrace.common.modal.*;
import io.etrace.common.modal.impl.TransactionImpl;
import io.etrace.common.util.JSONUtil;
import io.etrace.common.util.NetworkInterfaceHelper;
import io.etrace.common.util.ThreadUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class HeartbeatUploadTask implements Runnable {
    private static final int SENDER_TIMEOUT = 5000;
    // 由于etrace-agent可能同时存在于Xboot和直接用两种方式, 而这两种方式获取jar版本号的代码是不同的
    // 所以简单起见, 直接在这里写上版本号, 未来collector/consumer添加一个功能
    // 如果heartbeat status里没有这个值, 则表示是老版本etrace-agent
    // 以此来追踪所有agent的版本信息
    private static final String MIGRATION_ALI_VERSION = "3.0.0-ee-stable-SNAPSHOT";
    private final String HOST_IP;
    private final String HOST_NAME;
    private final Map<String, String> monitorErrorInfo = new HashMap<>();
    @Inject
    MessageManager messageManager;
    private List<Executor> executors;
    @Inject
    private MessageStats messageStats;
    @Inject
    private MetricStats metricStats;
    @Inject
    private EsightStats esightStats;
    private HeartbeatStats heartbeatStats;
    @Inject
    private MessageProducer producer;
    private volatile boolean active = true;
    private Client client = SocketClientFactory.getClient(SENDER_TIMEOUT);
    private JvmThreadExecutor jvmThreadExecutor;
    private EnvironmentConfig environmentConfig;
    private MessageHeader messageHeader;
    private MBeanExecutor mBeanExecutor;

    public HeartbeatUploadTask() {
        HOST_IP = NetworkInterfaceHelper.INSTANCE.getLocalHostAddress();
        HOST_NAME = NetworkInterfaceHelper.INSTANCE.getLocalHostName();
        messageHeader = new MessageHeader();
        messageHeader.setHostIp(HOST_IP);
        messageHeader.setHostName(HOST_NAME);
    }

    public void startup() {
        Thread t = new Thread(this, getName());
        t.setDaemon(true);
        t.start();
    }

    private void init() {
        heartbeatStats = messageStats.getHeartbeatStats();
        try {
            jvmThreadExecutor = new JvmThreadExecutor(HBConstants.JVM_THREAD_PREFIX);
            environmentConfig = new EnvironmentConfig(HBConstants.ENVIRONMENT);
            executors = new ArrayList<>();
            executors.add(new JvmExecutor(HBConstants.JAVA_PREFIX));
            mBeanExecutor = new MBeanExecutor();
        } catch (Exception ignore) {
            monitorErrorInfo.put(ErrorConstants.INIT, ignore.getMessage());
        }
    }

    public String getName() {
        return "HeartbeatUploadTask";
    }

    public void shutdown() {
        active = false;
    }

    @Override
    public void run() {
        init();
        long interval = 60 * 1000; // 60 seconds
        // try to wait trace query init success
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            return;
        }

        while (true) {
            Calendar cal = Calendar.getInstance();
            int second = cal.get(Calendar.SECOND);

            // try to avoid send heartbeat at 59-01 second
            if (second < 2 || second > 58) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore it
                }
            } else {
                break;
            }
        }

        Event event = Trace.newEvent("Reboot", HOST_IP);
        internalSend(event);
        int count = 0;
        while (active) {
            if (!client.openConnection()) {
                if (!active) {
                    break;
                }
                ThreadUtil.sleep(interval);
                continue;
            }
            if (count % 60 == 0) {
                //log env per one hour
                logEnvironment();
            }
            long start = System.currentTimeMillis();
            logStatus();
            logMBeanInfo();
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed < interval) {
                try {
                    Thread.sleep(interval - elapsed);
                } catch (InterruptedException e) {
                    break;
                }
            }
            count++;
        }
    }

    public void logEnvironment() {
        Heartbeat h = producer.newHeartbeat("Environment", HOST_IP);
        try {
            Map<String, String> values = environmentConfig.execute();
            if (values != null && values.size() > 0) {
                h.addTags(values);
            }
            h.setStatus(Message.SUCCESS);
        } catch (Throwable e) {
            monitorErrorInfo.put(ErrorConstants.LOG_ENVIRONMENT, e.getMessage());
            h.setStatus(e);
        } finally {
            internalSend(h);
        }
    }

    private void logStatus() {
        long step1 = System.currentTimeMillis();
        Transaction t = Trace.newTransaction("System", "Status");
        try {
            logExecutor();
            t.setStatus(Message.SUCCESS);
        } finally {
            t.setDuration(System.currentTimeMillis() - step1);
            internalSend(t);
        }

        long step2 = System.currentTimeMillis();
        Transaction t2 = Trace.newTransaction("System", "Thread-Dump");
        if (t2 instanceof TransactionImpl) {
            ((TransactionImpl)t2).setTimestamp(t.getTimestamp());
        }
        MessageStats nowStats = messageStats.copyStats();
        MetricStats nowMetricStats = metricStats.copyStats();
        try {
            logThreadExecutor();
            logMessageStats(nowStats, nowMetricStats);
            t2.setStatus(Message.SUCCESS);
        } finally {
            t2.setDuration(System.currentTimeMillis() - step2);
            boolean success = internalSend(t2);
            if (success) {
                monitorErrorInfo.clear();
                metricStats.resetToHistory(nowMetricStats);
                messageStats.resetToHistory(nowStats);
            }
        }

    }

    public void logMBeanInfo() {
        long start = System.currentTimeMillis();
        if (!mBeanExecutor.needMBean()) {
            return;
        }
        Map<String, List<Pair<String, String>>> mbeanInfos = mBeanExecutor.execute();
        if (mbeanInfos == null || mbeanInfos.isEmpty()) {
            return;
        }
        Transaction t = Trace.newTransaction("Platform", "MBean");
        try {
            mbeanInfos.forEach(this::mbeanInfo);
            t.setStatus(Message.SUCCESS);
        } catch (Throwable e) {
            monitorErrorInfo.put(ErrorConstants.LOG_MBEAN_EXECUTOR, e.getMessage());
        } finally {
            t.setDuration(System.currentTimeMillis() - start);
            internalSend(t);
        }
    }

    public void logExecutor() {
        Heartbeat h = producer.newHeartbeat(Constants.HEART_BEAT, HOST_IP);
        try {
            for (Executor executor : executors) {
                try {
                    Map<String, String> values = executor.execute();
                    if (values != null && values.size() > 0) {
                        h.addTags(values);
                    }
                } catch (Exception e) {
                    monitorErrorInfo.put(executor.type, e.getMessage());
                }
            }
            h.setStatus(Message.SUCCESS);
        } catch (Throwable e) {
            monitorErrorInfo.put(ErrorConstants.LOG_EXECUTOR, e.getMessage());
            h.setStatus(e);
        } finally {
            h.complete();
        }
    }

    private void logMessageStats(MessageStats messageStats, MetricStats metricStats) {
        Heartbeat heartbeatMsg = producer.newHeartbeat("agent-stat", HOST_IP);
        try {
            Map<String, Map<String, Object>> stats = new HashMap<>();
            stats.put("message-stats", messageStats.toStatMap());
            stats.put("metric-stats", metricStats.toStatMap());
            stats.put("esight-stats", esightStats.drainToMap());
            Map<String, Object> migrationStats = new HashMap<>();
            migrationStats.put("appid", AgentConfiguration.getServiceName());
            migrationStats.put("version", MIGRATION_ALI_VERSION);
            stats.put("migration-ali-stats", migrationStats);
            heartbeatMsg.setData(JSONUtil.toString(stats));
            heartbeatMsg.setStatus(Message.SUCCESS);
        } catch (Throwable e) {
            monitorErrorInfo.put(ErrorConstants.LOG_THREAD_EXECUTOR, e.getMessage());
            heartbeatMsg.setStatus(e);
        } finally {
            if (monitorErrorInfo.size() > 0) {
                heartbeatMsg.addTags(monitorErrorInfo);
            }
            heartbeatMsg.complete();
        }
    }

    private void logThreadExecutor() {
        Heartbeat threadDumpHB = producer.newHeartbeat("thread-dump", HOST_IP);
        try {
            threadDumpHB.setData(JSONUtil.toString(jvmThreadExecutor.execute()));
            threadDumpHB.setStatus(Message.SUCCESS);
        } catch (Throwable e) {
            monitorErrorInfo.put(ErrorConstants.LOG_THREAD_EXECUTOR, e.getMessage());
            threadDumpHB.setStatus(e);
        } finally {
            threadDumpHB.complete();
        }
    }

    private void mbeanInfo(String objectName, List<Pair<String, String>> attrs) {
        //mbean info
        Heartbeat h = producer.newHeartbeat("MBean", objectName);
        try {
            for (Pair<String, String> attr : attrs) {
                h.addTag(attr.getKey(), attr.getValue());
            }
            h.setStatus(Message.SUCCESS);
        } catch (Throwable e) {
            monitorErrorInfo.put(ErrorConstants.LOG_MBEAN_EXECUTOR, e.getMessage());
            h.setStatus(e);
        } finally {
            h.complete();
        }
    }

    private boolean internalSend(Message t) {
        try {
            t.setStatus(Constants.SUCCESS);
            CallStack callStack = new CallStack();
            callStack.setMessage(t);
            callStack.setRequestId(messageManager.getCurrentRequestId());
            callStack.setId(messageManager.getCurrentRpcId());
            callStack.setAppId(AgentConfiguration.getServiceName());
            callStack.setHostIp(HOST_IP);
            callStack.setHostName(HOST_NAME);
            //set live parameters: cluster, idc, ezone, ezoneid and also instance
            DiskFileConfiguration diskFileConfiguration = AgentConfiguration.getDiskFileConfiguration();
            callStack.setCluster(diskFileConfiguration.getCluster());
            callStack.setEzone(diskFileConfiguration.getEzone());
            callStack.setIdc(diskFileConfiguration.getIdc());
            callStack.setMesosTaskId(AgentConfiguration.getMesosTaskId());
            callStack.setEleapposLabel(AgentConfiguration.getEleapposLabel());
            callStack.setEleapposSlaveFqdn(AgentConfiguration.getEleapposSlaveFqdn());
            callStack.setInstance(AgentConfiguration.getInstance());
            JsonFactory jsonFactory = new JsonFactory();
            ByteArrayOutputStream baos = null;
            JsonGenerator generator = null;
            try {
                baos = new ByteArrayOutputStream();
                generator = jsonFactory.createJsonGenerator(baos, JsonEncoding.UTF8);
                generator.writeStartArray();
                JSONCodec.encodeAsArray(callStack, generator);
            } catch (IOException ignore) {
                monitorErrorInfo.put(ErrorConstants.CALLSTACK_TO_JSON, ignore.getMessage());
            } finally {
                if (generator != null) {
                    try {
                        generator.writeEndArray();
                        generator.close();
                    } catch (IOException ignore) {
                        monitorErrorInfo.put(ErrorConstants.CLOSE_GENERATOR, ignore.getMessage());
                    }
                }
            }
            messageHeader.setAppId(AgentConfiguration.getServiceName());
            messageHeader.setAst(System.currentTimeMillis());
            messageHeader.setInstance(AgentConfiguration.getInstance());
            boolean success = client.send(JSONUtil.toBytes(messageHeader), baos.toByteArray());
            if (!success) {
                return false;
            }
            heartbeatStats.incTotalSize(baos.size());
            heartbeatStats.incTotalCount(1);
            heartbeatStats.incSuccessCount(1);
            return true;
        } catch (Exception e) {
            monitorErrorInfo.put(ErrorConstants.INTERNAL_SEND, e.getMessage());
            return false;
        } finally {
            client.closeConnection();
            messageManager.reset();
        }
    }
}
