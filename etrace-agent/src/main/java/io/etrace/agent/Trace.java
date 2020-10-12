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

package io.etrace.agent;

import com.google.inject.Injector;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.agent.io.MessageSender;
import io.etrace.agent.io.SocketClientFactory;
import io.etrace.agent.message.callstack.CallstackProducer;
import io.etrace.agent.message.callstack.CallstackQueue;
import io.etrace.agent.message.metric.MetricProducer;
import io.etrace.agent.module.InjectorFactory;
import io.etrace.agent.monitor.HeartbeatUploadTask;
import io.etrace.agent.stat.CallstackStats;
import io.etrace.common.constant.Constants;
import io.etrace.common.message.metric.Counter;
import io.etrace.common.message.metric.Gauge;
import io.etrace.common.message.metric.Payload;
import io.etrace.common.message.metric.Timer;
import io.etrace.common.message.trace.Event;
import io.etrace.common.message.trace.TraceContext;
import io.etrace.common.message.trace.Transaction;

import java.util.Map;

/**
 * The main api of trace.
 * <p>
 * Limitation: Considering the trade-off between convenience of Trace api and the resource usage of agent application,
 * there are some default limitation on the length of names, the size of tags, the number of one Transaction's children
 * and etc. For example, if the length or the size exceeds the threshold, the excessive part will be ignored. Finally,
 * if you encounter any problems, go to https://github.com/etrace-io/etrace/issues for help
 * <p>
 * As for string length, when the size more than max size,it will be String.substring(0, maxSize). -- Trace: ---- type:
 * 256 char ---- name:  512 char ---- status: 64 char ---- data: 2 * 1024 char(default), adjustable ---- tag name: 64
 * char ---- tag value: 256 char(default), adjustable ---- tag size: 12(default), adjustable
 * <p>
 * -- Metric: ---- name: 512 char ---- tag name: 64 char(default), adjustable ---- tag value: 256 char(default) ,
 * adjustable ---- tag size: 8(default), adjustable
 * <p>
 * -- Redis size: ---- 500 redis("{url}{command}") every Transaction context
 * <p>
 * -- Metric Name: ---- 100 (default, adjustable) metric name every two seconds
 * <p>
 * ============================================================================================================
 * <p>
 * The Hierarchical Relation between service and service, thread and thread, service and thread , etc.
 * <p>
 * First, get the requestId ({@link Trace#getCurrentRequestId()} ) and rpcId ({@link Trace#nextLocalRpcId()}{@link
 * Trace#nextRemoteRpcId()}) after creating a new Transaction: Transaction t = Trace.newTransaction("Type", "Name");
 * String requestId = Trace.getCurrentRequestId(); String rpcId = Trace.nextLocalRpcId(); //local thread //String rpcId
 * = Trace.nextRemoteRpcId()  //remote server Second, transfer the requestId and rpcId to next service or thread. Then,
 * in the next service or thread, use {@link Trace#continueTrace(String, String)} to bind it :
 * Trace.continueTrace(requestId, rpcId); It succeeds in creating a Transaction, and you can get the hierarchical
 * relation information in ETrace.
 * ============================================================================================================
 * Redis stats: -- counter of redis calls = (succeedCount + failCount) -- cost time of redis calls = (durationSucceedSum
 * + durationFailSum) -- maxDuration, minDuration: the max(or min) cost time of redis calls
 * <p>
 * If fail to get the response, these values below are (-1) and the default is also (-1). -- responseCount,
 * responseSizeSum, maxResponseSize, minResponseSize -- hitCount : counter of response where the response hit is true
 */
public class Trace {
    private static Trace trace = new Trace();
    private CallstackProducer producer;
    private MetricProducer metricProducer;
    private CallstackQueue callstackQueue;
    private MessageSender tcpMessageSender;
    private HeartbeatUploadTask task;
    private CallstackStats callstackStats;

    private Trace() {
        Injector injector = InjectorFactory.getInjector();
        producer = injector.getInstance(CallstackProducer.class);
        callstackQueue = injector.getInstance(CallstackQueue.class);
        metricProducer = injector.getInstance(MetricProducer.class);
        tcpMessageSender = injector.getInstance(MessageSender.class);
        callstackStats = injector.getInstance(CallstackStats.class);
        task = injector.getInstance(HeartbeatUploadTask.class);
        task.startup();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdownTrace();
            SocketClientFactory.shutdown();
        }));
    }

    public static void shutdownTrace() {
        trace.task.shutdown();
        trace.producer.shutdown();
    }

    public static CallstackStats getStats() {
        return trace.callstackStats;
    }

    public static int getCallStackProducerQueueSize() {
        return trace.callstackQueue.getQueueSize();
    }

    public static int getTCPMessageSenderQueueSize() {
        return trace.tcpMessageSender.getQueueSize();
    }

    /**
     * Event event = Trace.newEvent("type", "name"); // create a new event try{ event.setData("data");  // set data
     * event.addTag("key","value"); // add tag event.setStatus(Constants.SUCCESS); //set status }finally {
     * event.complete(); // complete is necessary, otherwise the event is invalid. }
     *
     * @param type event type
     * @param name event name
     *             <p>
     *             废弃。不如使用logEvent方便。
     */
    @Deprecated
    public static Event newEvent(String type, String name) {
        return trace.producer.newEvent(type, name);
    }

    /**
     * create a event that the status is Constants.SUCCESS, and without setting data or tags.
     *
     * @param type the event type
     * @param name the event name
     */
    public static void logEvent(String type, String name) {
        logEvent(type, name, Constants.SUCCESS);
    }

    /**
     * @param type   the event type
     * @param name   the event name
     * @param status the event status
     */
    public static void logEvent(String type, String name, String status) {
        logEvent(type, name, status, null);
    }

    /**
     * @param type   the event type
     * @param name   the event name
     * @param status the event status
     * @param tags   the tags
     */
    public static void logEvent(String type, String name, String status, Map<String, String> tags) {
        logEvent(type, name, status, null, tags);
    }

    /**
     * @param type   the event type
     * @param name   the event name
     * @param status the event status
     * @param data   the data
     * @param tags   the tags
     */
    public static void logEvent(String type, String name, String status, String data, Map<String, String> tags) {
        trace.producer.logEvent(type, name, status, data, tags);
    }

    /**
     * create a new event, then set the throwable stack information to data, finally complete. it calls
     * Trace.logError(null, throwable), please look at the {@link Trace#logError(String, Throwable)}
     *
     * @param throwable the error Throwable
     */
    public static void logError(Throwable throwable) {
        logError(null, throwable);
    }

    public static void logError(Throwable throwable, Map<String, String> tags) {
        logError(null, throwable, tags);
    }

    /**
     * create a new event, then set the message and the throwable stack information, finally complete.
     * <p>
     * -- Event Type: if throwable instanceof ServiceException : type = "BusinessException" else if throwable instanceof
     * java.lang.Error : type = "Error" else if throwable instanceof java.lang.RuntimeException : type =
     * "RuntimeException" else type = "Exception"
     * <p>
     * -- Event Name: name = throwable.getClass().getName()
     * <p>
     * -- Status: status = "ERROR"
     * </p>
     *
     * @param message   the message to describe something
     * @param throwable exception
     */

    public static void logError(String message, Throwable throwable) {
        logError(message, throwable, null);
    }

    public static void logError(String message, Throwable throwable, Map<String, String> tags) {
        trace.producer.logError(message, throwable, tags);
    }

    /**
     * Transaction t = Trace.newTransaction("Type", "Name"); try { t.addTag("key","value"); // add tag //Do business
     * logic t.setStatus(Constants.SUCCESS); // please set status before return, and means it succeed to do the task. }
     * catch (InterruptedException e) { Trace.logError(e);  //  create a event to record the error t.setStatus(e); //
     * record transaction error status, means it catches the error. } finally { t.complete(); // It must complete
     * finally. }
     *
     * @param type the type
     * @param name the name
     */
    public static Transaction newTransaction(String type, String name) {
        return trace.producer.newTransaction(type, name);
    }

    /**
     * Whether there is any Transaction that hasn't completed in the local thread context.
     *
     * @return boolean
     */
    public static boolean hasTransaction() {
        return trace.producer.hasTransaction();
    }

    /**
     * Clear all trace stacks and context in the local thread. In only want to clear context, call {@link
     * Trace#removeContext()}
     */
    public static void clean() {
        trace.producer.clean();
    }

    /**
     * Whether there is any Context in the local thread
     *
     * @return boolean
     */
    public static boolean hasContext() {
        return trace.producer.hasContext();
    }

    /**
     * Bind the requestId and rpcId of previous service or thread to the present More details, please see the
     * Hierarchical Relation : {@link Trace}
     *
     * @param requestId requestId
     * @param rpcId     rpcId
     */
    public static void continueTrace(String requestId, String rpcId) {
        trace.producer.continueTrace(requestId, rpcId);
    }

    /**
     * Get the current request id the rule of request id : {serviceName}^^{random long or uuid}|{timestamp} serviceName
     * = serviceName (first) or appId (second) or unknown (last) such as : arch
     * .etrace^^-1018579420457251359|1503388031849
     * more detail about RpcId
     *
     * @return {@link String}
     */
    public static String getCurrentRequestId() {
        return trace.producer.getCurrentRequestId();
    }

    /**
     * @return {@link String}
     */
    public static String getRpcId() {
        return trace.producer.getRpcId();
    }

    /**
     * 已修复成 getRpcId()
     *
     * @return {@link String}
     */
    public static String getCurrentRpcId() {
        return trace.producer.getRpcId();
    }

    /**
     * return rpcId + "." + currentCall deprecated. use getRpcId() instead. more detail about RpcId, refer to
     * <p>
     * 在某个场景：前文调用了nextRemoteRpcId()后，再调用getCurrentRpcId()返回的rpcId是下一层次的（即与Remote Call同级），实际上应是当前层次。
     * 可能会造成显式传递RpcId时，层次错误。 另一个api: getRpcId()修复了这个问题。
     *
     * @return {@link String}
     */
    public static String getCurrentRpcIdAndCurrentCall() {
        return trace.producer.getCurrentRpcIdAndCurrentCall();
    }

    /**
     * remove the local thread context if it exists.
     */
    public static void removeContext() {
        trace.producer.removeContext();
    }

    public static String getCurrentRpcIdWithAppId() {
        return AgentConfiguration.getAppId() + "|" + getCurrentRpcId();
    }

    /**
     * get next remote rpc id the rule of rpc id : {@link Trace#getRpcId()}
     *
     * @return String a rpc id
     */
    public static String nextRemoteRpcId() {
        String nextRpcId = trace.producer.nextRemoteRpcId();
        logEvent(Constants.TYPE_ETRACE_LINK, Constants.NAME_REMOTE_CALL, Constants.SUCCESS, nextRpcId, null);
        return nextRpcId;
    }

    /**
     * get next thread rpc id rpc id rule : {@link Trace#getRpcId()}
     *
     * @return String a rpc id
     */
    public static String nextLocalRpcId() {
        String nextRpcId = trace.producer.nextLocalRpcId();
        logEvent(Constants.TYPE_ETRACE_LINK, Constants.NAME_ASYNC_CALL, Constants.SUCCESS, nextRpcId, null);
        return nextRpcId;
    }

    /**
     * get the appId from local thread context if local thread context is null (sometimes it means you don't have any
     * Transaction in your context), it is going to return "unknown"
     *
     * @return String appId
     */
    public static String getClientAppId() {
        return trace.producer.getClientAppId();
    }

    public static TraceContext exportContext() {
        return trace.producer.exportContext();
    }

    public static void importContext(TraceContext context) {
        trace.producer.importContext(context);
    }

    public static boolean isImportContext() {
        return trace.producer.isImportContext();
    }

    public static void logHeartbeat(String type, String name, String status, String
        data, Map<String, String> tags) {
        trace.producer.logHeartbeat(type, name, status, data, tags);
    }

    /**
     * Counter counter = Trace.newCounter("metric-name"); //create a new Counter counter.addTag("key-1", "value-1"); //
     * add tag counter.once(); // set value=1, when call once, the Counter finishes, refer to: {@link Counter#once()}
     * <p>
     * Another way： //when call value(10), the Counter ends, refer to : {@link Counter#value(long)}
     * Trace.newCounter("metric-name").addTag("key-1", "value-1").value(10);
     *
     * @param name the metric name
     * @return Counter
     */
    public static Counter newCounter(String name) {
        return trace.metricProducer.newCounter(name);
    }

    /**
     * Gauge gauge = Trace.newGauge("metric-name"); //create a new Gauge gauge.addTag("key-1",
     * "value-1").addTag("key-2", "value-2"); //add tag gauge.value(200); //set value=200, when call value(200), the
     * Gauge finishes, refer to : {@link Gauge#value(double)}
     * <p>
     * Another way: // when call value(100) , the Gauge finishes, refer to : {@link Gauge#value(double)}
     * Trace.newGauge("metric-name").addTag("key-1", "value-1").value(100);
     *
     * @param name the metric name
     * @return Gauge
     */
    public static Gauge newGauge(String name) {
        return trace.metricProducer.newGauge(name);
    }

    /**
     * The timer doesn't support the quantile(upper_85,upper_90 etc.), but another metric type : Histogram do.
     * timer.setUpperEnable(false), means you get the metric type : Timer. Otherwise, it's Histogram.(default)
     * <p>
     * For example: Timer timer = Trace.newTimer("metric-name"); //create a new Timer, the start time and time point is
     * (System.currentTimeMillis()) timer.addTag("key-1", "value-1");   //add tag timer.addTag("key-2", "value-2");
     * //add tag //timer.setUpperEnable(false); //set the quantile enable, default : true //do business logic
     * timer.end(); //the timer finishes. the cost time = (end time - start time)ms, the count = 1, refer to : {@link
     * Timer#end()}
     * <p>
     * Another way: // when call value(40) , the Timer finishes, the cost time = 40ms, the count = 1, refer to : {@link
     * Timer#value(long)} Timer time = Trace.newTimer("metric-name").addTag("key-1", "value-1").addTag("key-2",
     * "value-2").value(40);
     *
     * @param name the metric name
     * @return Timer
     */
    public static Timer newTimer(String name) {
        return trace.metricProducer.newTimer(name);
    }

    /**
     * Payload payload = Trace.newPayload("metric-name"); //create a new Payload, the time point is
     * (System.currentTimeMillis()) payload.addTag("key-1", "value-1").addTag("key-2", "value-2"); //add tag
     * payload.value(1000); //set value = 1000, count = 1, and the Payload finishes, refer to : {@link
     * Payload#value(long)}
     * <p>
     * Another way: // when call value(2000), the Payload finishes. Trace.newPayload("metric-name").addTag("key-1",
     * "value-1").value(2000);
     *
     * @param name the metric name
     * @return Timer
     */
    public static Payload newPayload(String name) {
        return trace.metricProducer.newPayload(name);
    }
}
