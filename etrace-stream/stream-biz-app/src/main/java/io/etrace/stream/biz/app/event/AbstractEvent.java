package io.etrace.stream.biz.app.event;

import com.google.common.base.Strings;
import io.etrace.common.constant.Constants;
import io.etrace.stream.biz.app.CallStackHelper;
import io.etrace.stream.biz.app.EventConstant;
import io.etrace.stream.core.model.Event;
import io.etrace.stream.core.model.Header;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public abstract class AbstractEvent implements Event {
    protected long timestamp;
    private Header header;
    private String type;
    private String name;
    private String status;
    private Map<String, String> tags;
    private String data;

    public AbstractEvent(String type, String name, long timestamp, String status, Map<String, String> tags) {
        this.type = type;
        this.name = name;
        this.timestamp = timestamp;
        this.status = CallStackHelper.transferEventStatus(status);
        this.tags = tags;
    }

    @Override
    public Header getHeader() {
        return header;
    }

    @Override
    public void setHeader(Header header) {
        this.header = header;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public void addTag(String key, String value) {
        if (tags == null) {
            tags = newHashMap();
        }
        tags.put(key, value);
    }

    // return unknown if absent
    public String getTag(String key) {
        if (tags == null) {
            return Constants.UNKNOWN;
        }
        return tags.getOrDefault(key, Constants.UNKNOWN);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String getEventType() {
        return EventConstant.EVENT_TYPE_APP;
    }

    @Override
    public String shardingKey() {
        return type + name;
    }

    public String simplify(String raw) {
        if (Strings.isNullOrEmpty(raw)) {
            return raw;
        }

        int index = raw.indexOf("?");
        if (index >= 0) {
            raw = raw.substring(0, index);
        }

        return raw;
    }
}

