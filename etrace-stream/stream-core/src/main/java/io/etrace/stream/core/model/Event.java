package io.etrace.stream.core.model;

import io.etrace.common.pipeline.Filterable;

import java.util.List;

public interface Event extends Filterable {
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

    @Override
    default String filterKey() {
        return getEventType();
    }
}
