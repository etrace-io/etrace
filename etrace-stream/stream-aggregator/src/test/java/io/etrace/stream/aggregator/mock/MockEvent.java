package io.etrace.stream.aggregator.mock;

import io.etrace.common.constant.Constants;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class MockEvent {
    private String name;
    private long time;
    private long value;
    private Map<String, String> tags;
    private String msg;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public String getTag(String key) {
        if (tags == null) {
            return Constants.UNKNOWN;
        }
        return tags.getOrDefault(key, Constants.UNKNOWN);
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void addTag(String key, String value) {
        if (tags == null) {
            tags = newHashMap();
        }

        tags.put(key, value);
    }

    @Override
    public String toString() {
        return "MockEvent{" +
            "name='" + name + '\'' +
            ", time=" + time +
            ", value=" + value +
            ", tags=" + tags +
            ", msg='" + msg + '\'' +
            '}';
    }
}
