package io.etrace.consumer.storage.hbase;

import org.apache.hadoop.hbase.client.Put;

import java.util.List;

public interface IHBaseClient {
    boolean executeBatch(String logicTableName, String physicalTableName, List<Put> actions);
}
