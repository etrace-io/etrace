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

package io.etrace.agent.monitor;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import io.etrace.agent.Trace;
import io.etrace.agent.Version;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.agent.message.callstack.CallstackProducer;
import io.etrace.agent.message.heartbeat.HeartbeatQueue;
import io.etrace.agent.monitor.jvm.EnvironmentExecutor;
import io.etrace.agent.monitor.jvm.JvmHeartBeatExecutor;
import io.etrace.agent.monitor.jvm.JvmThreadHeartBeatExecutor;
import io.etrace.agent.monitor.mbean.MBeanExecutor;
import io.etrace.agent.stat.CallstackStats;
import io.etrace.agent.stat.HeartbeatStats;
import io.etrace.agent.stat.MetricStats;
import io.etrace.common.constant.Constants;
import io.etrace.common.message.trace.*;
import io.etrace.common.message.trace.impl.TransactionImpl;
import io.etrace.common.util.JSONUtil;
import io.etrace.common.util.NetworkInterfaceHelper;
import io.etrace.common.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HeartbeatUploadTask {
    private final String HOST_IP = NetworkInterfaceHelper.INSTANCE.getLocalHostAddress();
    private final Map<String, String> monitorErrorInfo = new HashMap<>();
    @Inject
    TraceManager traceManager;
    private String INIT = "Init";
    private String INTERNAL_SEND = "Send_to_collector";
    private String CALLSTACK_TO_JSON = "CallStack_to_json";
    private String CLOSE_GENERATOR = "Close_generator";
    private String LOG_ENVIRONMENT = "Environment";
    private String LOG_EXECUTOR = "Executors";
    private String LOG_THREAD_EXECUTOR = "Thread-dump";
    private String LOG_MBEAN_EXECUTOR = "Mbean";
    private List<HeartBeatExecutor> heartBeatExecutors;
    @Inject
    private CallstackStats callstackStats;
    @Inject
    private MetricStats metricStats;

    private HeartbeatStats heartbeatStats;
    @Inject
    private CallstackProducer producer;

    @Inject
    private HeartbeatQueue heartbeatQueue;

    private JvmThreadHeartBeatExecutor jvmThreadExecutor;
    private EnvironmentExecutor environmentExecutor;
    private MBeanExecutor mBeanExecutor;
    private ScheduledExecutorService executorService;

    /*
    default initial delay = 10s
     */
    private Long heartbeatDelay = 10L;

    /*
    default upload interval = 60s
     */
    private Long heartbeatInterval = 60L;
    private int count = 0;
    private boolean rebootEventSent = false;

    public HeartbeatUploadTask(Long heartbeatDelayInSecond, Long heartbeatIntervalInSecond) {
        this.heartbeatDelay = heartbeatDelayInSecond;
        this.heartbeatInterval = heartbeatIntervalInSecond;
    }

    public HeartbeatUploadTask() {
    }

    public void startup() {
        initExecutors();

        executorService = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("HeartbeatUploadTask").build());

        executorService.scheduleAtFixedRate(this::uploadStat, heartbeatDelay, heartbeatInterval, TimeUnit.SECONDS);
    }

    private void logRebootEvent() {
        Event event = Trace.newEvent("Reboot", HOST_IP);
        event.setStatus(Constants.SUCCESS);
        internalSend(event);
    }

    private void initExecutors() {
        heartbeatStats = callstackStats.getHeartbeatStats();
        try {
            jvmThreadExecutor = new JvmThreadHeartBeatExecutor(HeartBeatConstants.JVM_THREAD_PREFIX);
            environmentExecutor = new EnvironmentExecutor(HeartBeatConstants.ENVIRONMENT);
            heartBeatExecutors = Lists.newArrayList(new JvmHeartBeatExecutor(HeartBeatConstants.JAVA_JVM));
            mBeanExecutor = new MBeanExecutor();
        } catch (Exception ignore) {
            monitorErrorInfo.put(INIT, ignore.getMessage());
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }

    private void uploadStat() {
        if (!rebootEventSent) {
            logRebootEvent();
            rebootEventSent = true;
        }
        //log env per one hour
        if (count % 60 == 0) {
            logEnvironment();
            count = 0;
        }
        logMBeanInfo();
        logStatus();
        count++;
    }

    public void logEnvironment() {
        Heartbeat h = producer.newHeartbeat("Environment", HOST_IP);
        try {
            Map<String, String> values = environmentExecutor.execute();
            // ignore tht tag limit
            if (values != null && values.size() > 0) {
                h.addTags(values);
            }
            h.setStatus(Message.SUCCESS);
        } catch (Throwable e) {
            monitorErrorInfo.put(LOG_ENVIRONMENT, e.getMessage());
            h.setStatus(e);
        } finally {
            internalSend(h);
        }
    }

    private void logStatus() {
        long step1 = System.currentTimeMillis();
        Transaction statusTransaction = Trace.newTransaction("System", "Status");
        try {
            logExecutor();
            statusTransaction.setStatus(Message.SUCCESS);
        } finally {
            statusTransaction.setDuration(System.currentTimeMillis() - step1);
            internalSend(statusTransaction);
        }

        long step2 = System.currentTimeMillis();
        Transaction threadDumpTransaction = Trace.newTransaction("System", "Thread-Dump");
        if (threadDumpTransaction instanceof TransactionImpl) {
            ((TransactionImpl)threadDumpTransaction).setTimestamp(statusTransaction.getTimestamp());
        }
        CallstackStats nowStats = callstackStats.copyStats();
        MetricStats nowMetricStats = metricStats.copyStats();
        try {
            logThreadExecutor();
            logMessageStats(nowStats, nowMetricStats);
            threadDumpTransaction.setStatus(Message.SUCCESS);
        } finally {
            threadDumpTransaction.setDuration(System.currentTimeMillis() - step2);
            // todo： 这里不能简单调用  threadDumpTransaction.complete(); 否则会多flush一条数据出去
            // 但是问题是，这里不complete，就是未complete的数据了
            // threadDumpTransaction.complete();

            boolean success = internalSend(threadDumpTransaction);
            if (success) {
                monitorErrorInfo.clear();
                metricStats.resetToHistory(nowMetricStats);
                callstackStats.resetToHistory(nowStats);
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
            monitorErrorInfo.put(LOG_MBEAN_EXECUTOR, e.getMessage());
        } finally {
            t.setDuration(System.currentTimeMillis() - start);
            internalSend(t);
        }
    }

    public void logExecutor() {
        Heartbeat h = producer.newHeartbeat(Constants.HEART_BEAT, HOST_IP);
        try {
            for (HeartBeatExecutor heartBeatExecutor : heartBeatExecutors) {
                try {
                    Map<String, String> values = heartBeatExecutor.execute();
                    if (values != null && values.size() > 0) {
                        h.addTags(values);
                    }
                } catch (Exception e) {
                    monitorErrorInfo.put(heartBeatExecutor.type, e.getMessage());
                }
            }
            h.setStatus(Message.SUCCESS);
        } catch (Throwable e) {
            monitorErrorInfo.put(LOG_EXECUTOR, e.getMessage());
            h.setStatus(e);
        } finally {
            h.complete();
        }
    }

    private void logMessageStats(CallstackStats callstackStats, MetricStats metricStats) {
        Heartbeat heartbeatMsg = producer.newHeartbeat("agent-stat", HOST_IP);
        try {
            Map<String, Map<String, Object>> stats = new HashMap<>();
            stats.put("message-stats", callstackStats.toStatMap());
            stats.put("metric-stats", metricStats.toStatMap());

            //这里不需要上传migrationStats，不过可以上传应用相关的信息: appId, version, host, ip, ect.
            Map<String, Object> migrationStats = new HashMap<>();
            migrationStats.put("appid", AgentConfiguration.getAppId());
            migrationStats.put("version", Version.TraceVersion);
            stats.put("app-stats", migrationStats);

            heartbeatMsg.setData(JSONUtil.toString(stats));
            heartbeatMsg.setStatus(Message.SUCCESS);
        } catch (Throwable e) {
            monitorErrorInfo.put(LOG_THREAD_EXECUTOR, e.getMessage());
            heartbeatMsg.setStatus(e);
        } finally {
            if (monitorErrorInfo.size() > 0) {
                heartbeatMsg.addTags(monitorErrorInfo);
            }
            heartbeatMsg.complete();
        }
    }

    private void logThreadExecutor() {
        Heartbeat threadDump = producer.newHeartbeat("thread-dump", HOST_IP);
        try {
            threadDump.setData(JSONUtil.toString(jvmThreadExecutor.execute()));
            threadDump.setStatus(Message.SUCCESS);
        } catch (Throwable e) {
            monitorErrorInfo.put(LOG_THREAD_EXECUTOR, e.getMessage());
            threadDump.setStatus(e);
        } finally {
            threadDump.complete();
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
            monitorErrorInfo.put(LOG_MBEAN_EXECUTOR, e.getMessage());
            h.setStatus(e);
        } finally {
            h.complete();
        }
    }

    private boolean internalSend(Message t) {
        boolean result = heartbeatQueue.produce(traceManager.getCurrentRequestId(), traceManager.getRpcId(), t);

        heartbeatStats.incTotalCount(1);
        if (result) {
            heartbeatStats.incSuccessCount(1);
        }
        traceManager.reset();
        return result;
    }
}
