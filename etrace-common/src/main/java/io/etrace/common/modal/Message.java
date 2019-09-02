package io.etrace.common.modal;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.util.Map;

public interface Message {
    String SUCCESS = "0";

    long getId();

    void setId(long id);

    Map<String, String> getTags();

    void setTags(Map<String, String> tags);

    void addTag(String key, String value);

    String getType();

    void setType(String type);

    String getName();

    void setName(String name);

    void complete();

    boolean isCompleted();

    String getStatus();

    void setStatus(String status);

    void setStatus(Throwable e);

    long getTimestamp();

    default public void encodeMessageAsArray(JsonGenerator jg) throws IOException {
    }

}
