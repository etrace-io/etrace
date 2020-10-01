package io.etrace.stream.biz.app.event;

import java.util.Map;

import static io.etrace.common.constant.Constants.SQL_DATABASE;
import static io.etrace.common.constant.Constants.UNKNOWN;

public class SQL extends AbstractDependencyTransaction {
    private String database = UNKNOWN;
    private String operation = UNKNOWN;
    private String table = UNKNOWN;

    public SQL(String type, String name, long timestamp, String status, Map<String, String> tags, long duration,
               String soaServiceMethod, String rmqConsumerQueue) {
        super(type, name, timestamp, status, tags, duration, soaServiceMethod, rmqConsumerQueue);

        int tableIndex = name.indexOf(".");
        if (tableIndex != -1) {
            this.table = name.substring(0, tableIndex);
            this.operation = name.substring(tableIndex + 1);
        }
        if (tags != null) {
            this.database = tags.getOrDefault(SQL_DATABASE, UNKNOWN);
        }
    }

    public String getDatabase() {
        return database;
    }

    public String getOperation() {
        return operation;
    }

    public String getTable() {
        return table;
    }

    @Override
    public String shardingKey() {
        return getHeader().getAppId() + database + table;
    }
}
