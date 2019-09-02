package io.etrace.common.modal;

import java.util.Map;

public interface Heartbeat extends Message {
    String getData();

    void setData(String data);

    void addTags(Map<String, String> tags);
}
