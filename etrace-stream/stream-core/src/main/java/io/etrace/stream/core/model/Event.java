package io.etrace.stream.core.model;

import java.util.List;

public interface Event {
    Header getHeader();

    void setHeader(Header header);

    String shardingKey();

    String getEventType();

    long getTimestamp();

    /**
     * 解析完成后做的一些额外的工作, 子类可根据需要自行重写
     */
    default List<Event> postDecode() {
        return null;
    }
}
