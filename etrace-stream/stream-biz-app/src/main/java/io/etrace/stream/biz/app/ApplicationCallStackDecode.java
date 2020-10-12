package io.etrace.stream.biz.app;

import com.google.common.collect.Lists;
import io.etrace.stream.biz.app.event.Exception;
import io.etrace.stream.biz.app.event.*;
import io.etrace.stream.core.util.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.collect.Sets.newHashSet;
import static io.etrace.stream.biz.app.EventConstant.*;

public class ApplicationCallStackDecode extends AbstractCallStackDecode {
    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationCallStackDecode.class);
    private final static Pattern REMOVE_SPACE = Pattern.compile(" ");
    private final Set<String> exceptionTypes = newHashSet();

    public ApplicationCallStackDecode() {
        super();
        exceptionTypes.add("Exception");
        exceptionTypes.add("RuntimeException");
        exceptionTypes.add("Error");
        exceptionTypes.add("BusinessException");
        exceptionTypes.add("Throwable");
    }

    @Override
    public Object parseEvent(String type, String name, long timestamp, String status, Map<String, String> tags,
                             String data) {
        if (exceptionTypes.contains(type)) {
            return new Exception(type, name, timestamp, status, tags);
        } else {
            return new Event(type, name, timestamp, status, tags);
        }
    }

    @Override
    public Object parseTransaction(String type, String name, long timestamp, String status, Map<String, String> tags,
                                   long duration, String soaServiceMethod, String rmqConsumerQueue) {
        if (TRANSACTION_TYPE_URL.equals(type)) {
            return new URL(type, name, timestamp, status, tags, duration);
        } else if (TRANSACTION_TYPE_SQL.equals(type)) {
            return new SQL(type, name, timestamp, status, tags, duration, soaServiceMethod, rmqConsumerQueue);
        } else {
            return new Transaction(type, name, timestamp, status, tags, duration);
        }
    }

    @Override
    public Object parseHeartbeat(String type, String name, long timestamp, String status, Map<String, String> tags,
                                 String data) {
        if (EventConstant.HEARTBEAT_TYPE_HEARTBEAT.equals(type)) {
            List<AbstractJVM> result = Lists.newArrayList();
            if (!Objects.isNull(tags)) {
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (key == null || value == null) {
                        continue;
                    }
                    AbstractJVM event = newJVMEvent(timestamp, key, value);
                    if (event != null) {
                        result.add(event);
                    }
                }
            }
            return result;
        } else {
            return null;
        }
    }

    private AbstractJVM newJVMEvent(long timestamp, String key, String value) {
        if (key == null || value == null) {
            return null;
        }
        AbstractJVM jvmEvent;
        if (key.contains(JVM_MEMORY_POOL)) {
            jvmEvent = new JVMMemoryPool();
        } else {
            jvmEvent = new JVM();
        }
        jvmEvent.setTimestamp(timestamp);
        jvmEvent.setValue(ObjectUtil.toDouble(value));
        if (key.contains(" ")) {
            key = REMOVE_SPACE.matcher(key).replaceAll("");
        }
        if (key.contains(JVM_MEMORY_POOL)) {
            jvmEvent.setType(JVM_MEMORY_POOL);
            String name = key.substring(JVM_MEMORY_POOL.length() + 1);
            int index = name.indexOf(".");
            if (index != -1) {
                jvmEvent.setName(name.substring(0, index));
                if (jvmEvent instanceof JVMMemoryPool) {
                    ((JVMMemoryPool)jvmEvent).setSubType(name.substring(index + 1));
                }
            }
        } else if (key.contains(JVM_MEMORY_HEAP)) {
            jvmEvent.setType(JVM_MEMORY_HEAP);
            jvmEvent.setName(key.substring(JVM_MEMORY_HEAP.length() + 1));
        } else if (key.contains(JVM_GARBAGE_COUNT)) {
            jvmEvent.setType(JVM_GARBAGE_COUNT);
            jvmEvent.setName(key.substring(JVM_GARBAGE_COUNT.length() + 1));
        } else if (key.contains(JVM_GARBAGE_TIME)) {
            jvmEvent.setType(JVM_GARBAGE_TIME);
            jvmEvent.setName(key.substring(JVM_GARBAGE_TIME.length() + 1));
        } else if (key.contains(JVM_THREAD)) {
            jvmEvent.setType(JVM_THREAD);
            jvmEvent.setName(key.substring(JVM_THREAD.length() + 1));
        } else if (key.contains(JVM_CPU)) {
            jvmEvent.setType(JVM_CPU);
            jvmEvent.setName(key.substring(JVM_CPU.length() + 1));
        } else if (key.contains(JVM_LOADED_CLASS)) {
            jvmEvent.setType(JVM_LOADED_CLASS);
            jvmEvent.setName("classes");
        }
        return jvmEvent;
    }

    @Override
    public void postParseMessage(io.etrace.stream.core.model.Event event, io.etrace.stream.core.model.Event parent,
                                 io.etrace.stream.core.model.Event root, String appId) {
        if (event instanceof Exception) {
            Exception exception = (Exception)event;
            exception.setMethod("UNKNOWN");
            exception.setSourceType("UNKNOWN");
        }
    }

    @Override
    public Object parseNewEvent(io.etrace.stream.core.model.Event event, io.etrace.stream.core.model.Event parent,
                                io.etrace.stream.core.model.Event root, String appId) {
        return null;
    }
}
