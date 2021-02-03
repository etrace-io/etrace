package io.etrace.consumer.storage.hbase;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.ResultScanner;

import java.io.IOException;
import java.util.List;

public interface IHBaseClientFactory {

    boolean tableExists(final TableName tableName);

    List<byte[]> getColumnFamilyByTable(String table);

    /**
     * region size 会尝试写入 table description中的key_region_size，来实现动态调整region size的功能。
     */
    HTable getOrCreateTable(String logicTableName, String physicalTableName) throws IOException;

    /**
     * get table to query data
     */
    HTable getTableByPhysicalName(String physicalTableName) throws IOException;

    void deleteTable(String tableName) throws IOException;

    /**
     * 根据 table和 table的region size 计算出shard。放到 rowkey的前缀中，来做balance region size 会尝试从 table
     * description中的key_region_size去读取，来实现动态调整region size的功能。
     */
    short getShardIdByPhysicalTableName(String physicalTableName, int hashcode);

    /**
     * logicalName的表名
     */
    short getShardIdByLogicalTableName(String logicalTableName, long timestamp, int hashcode);

    void closeCurrentThreadHTable(String tableName);

    void closeHTable(HTable table);

    void closeScanner(ResultScanner resultScanner);
}
