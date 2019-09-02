package io.etrace.common.modal;

import com.fasterxml.jackson.core.JsonGenerator;
import io.etrace.common.Constants;
import io.etrace.common.message.MessageManager;
import io.etrace.common.util.MessageHelper;
import io.etrace.common.util.SimpleArrayMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMessage implements Message {
    public static final int TYPE_TRUNCATE_SIZE = 256;
    public static final int NAME_TRUNCATE_SIZE = 512;
    public static final int STATUS_TRUNCATE_SIZE = 64;
    public static final int TAG_KEY_TRUNCATE_SIZE = 64;
    protected String type;
    protected String name;
    protected String status = Constants.UNSET;
    protected long timestamp;
    protected boolean completed;
    protected Map<String, String> tags;
    protected MessageManager manager;
    protected long id;

    protected AbstractMessage() {

    }

    public AbstractMessage(String type, String name, MessageManager manager) {
        this.type = type;
        this.name = name;
        this.manager = manager;
        timestamp = System.currentTimeMillis();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    protected void addTagsForJsonDecode(Map<String, String> tags) {
        if (this.tags == null) {
            this.tags = new HashMap<>();
        }
        this.tags.putAll(tags);
    }

    protected void addTags(Map<String, String> tags) {
        if (completed) {
            return;
        }
        if (this.tags == null) {
            this.tags = new HashMap<>();
        }
        this.tags.putAll(tags);
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    protected void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public void setTags(Map<String, String> tags) {
        if (tags != null && tags.size() > 0) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                addTag(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void addTag(String key, String value) {
        if (completed) {
            return;
        }
        try {
            if (manager != null) {
                int tagCount = this.manager.getConfigManager().getTagCount();
                if (tags == null) {
                    tags = new SimpleArrayMap<>(tagCount);
                }
                tags.put(key, value);
            } else {
                if (tags == null) {
                    tags = new SimpleArrayMap<>(8);
                }
                tags.put(key, value);
            }
        } catch (Exception ignore) {
        }
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void setStatus(Throwable e) {
        status = e.getClass().getName();
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public void encodeMessageAsArray(JsonGenerator jg) throws IOException {
        Message message = this;
        jg.writeStartArray();
        boolean isRedisTransaction = false;
        if (message instanceof Event) {
            jg.writeString("event");
        } else if (message instanceof Transaction) {
            jg.writeString("transaction");
            isRedisTransaction = Constants.REDIS_TYPE.equals(message.getType()) && Constants.REDIS_NAME.equals(
                message.getName());
        } else if (message instanceof Heartbeat) {
            jg.writeString("heartbeat");
        }
        jg.writeString(MessageHelper.truncate(message.getType(), TYPE_TRUNCATE_SIZE));
        jg.writeString(MessageHelper.truncate(message.getName(), NAME_TRUNCATE_SIZE));
        jg.writeString(MessageHelper.truncate(message.getStatus(), STATUS_TRUNCATE_SIZE));
        jg.writeNumber(message.getId());
        jg.writeNumber(message.getTimestamp());
        jg.writeBoolean(message.isCompleted());
        if (isRedisTransaction) {
            JSONCodec.writeRedisStats(jg, (Transaction)message);
        } else if (message.getTags() != null && message.getTags().size() > 0) {
            jg.writeStartObject();
            for (Map.Entry<String, String> entry : message.getTags().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (manager != null) {
                    int tagSize = this.manager.getConfigManager().getTagSize();
                    key = MessageHelper.truncate(key, TAG_KEY_TRUNCATE_SIZE);
                    value = MessageHelper.truncate(value, tagSize);
                }
                jg.writeStringField(key, value);
            }
            jg.writeEndObject();
        } else {
            jg.writeNull();
        }
        if (message instanceof Event) {
            jg.writeString(((Event)message).getData());
        } else if (message instanceof Transaction) {
            Transaction transaction = (Transaction)message;
            jg.writeNumber(transaction.getDuration());
            if (transaction.getChildren() != null && transaction.getChildren().size() > 0 && !isRedisTransaction) {
                jg.writeStartArray();
                for (Message child : transaction.getChildren()) {
                    child.encodeMessageAsArray(jg);
                }
                jg.writeEndArray();
            } else {
                jg.writeNull();
            }
        } else if (message instanceof Heartbeat) {
            jg.writeString(((Heartbeat)message).getData());
        }

        jg.writeEndArray();
    }

}
