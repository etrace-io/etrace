package io.etrace.stream.biz.app.event;

import io.etrace.stream.biz.app.URLPatternParser;

import java.util.Map;

public class URL extends AbstractTransaction {
    private String url;

    public URL(String type, String name, long timestamp, String status, Map<String, String> tags, long duration) {
        super(type, name, timestamp, status, tags, duration);

        this.url = URLPatternParser.parseURL(name);
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String shardingKey() {
        return getHeader().getAppId() + url;
    }

}
