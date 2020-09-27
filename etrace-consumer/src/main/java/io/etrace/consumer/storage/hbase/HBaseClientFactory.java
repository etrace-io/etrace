/*
 * Copyright 2019 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.consumer.storage.hbase;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.hash.Hashing;
import io.etrace.consumer.config.ConsumerProperties;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.regionserver.BloomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;

@Component
public class HBaseClientFactory {
    private final Logger LOGGER = LoggerFactory.getLogger(HBaseClientFactory.class);

    private final short defaultRegionNum = 60;
    private final Object lock = new Object();
    @Autowired
    private ConsumerProperties consumerProperties;
    private ConcurrentSkipListMap<Long, Short> stackRegions;
    private Configuration configuration;
    private Connection connection;
    private Map<String, Boolean> tableExist = new ConcurrentHashMap<>();
    private Cache<String, ThreadLocal<Table>> tableCache = CacheBuilder.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .removalListener((RemovalListener<String, ThreadLocal<Table>>)removalNotification -> {
            if (null != removalNotification.getValue() && null != removalNotification.getValue().get()) {
                try {
                    removalNotification.getValue().get().close();
                } catch (Exception e) {
                    LOGGER.error("close hbase table  error,  is {}", removalNotification.getKey(), e);
                }
            }
        }).build();

    @PostConstruct
    public void startup() {
        configuration = new Configuration();
        configuration.addResource("hbase-site-default.xml");
        configuration.addResource("hbase-site.xml");
        try {
            connection = ConnectionFactory.createConnection(configuration);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create HBase pool");
        }

        stackRegions = new ConcurrentSkipListMap<>();
        for (ConsumerProperties.Table table : consumerProperties.getHbase()) {
            if (table.getTable().equalsIgnoreCase("stack")) {
                for (ConsumerProperties.Shard shard : table.getDistribution()) {
                    LocalDate localDate = LocalDate.parse(shard.getTime(), DateTimeFormatter.ISO_DATE);
                    Long mills = LocalDateTime.of(localDate, LocalTime.ofNanoOfDay(0)).atZone(ZoneOffset.UTC)
                        .toInstant().toEpochMilli();
                    stackRegions.put(mills, shard.getRegion());
                }
            }
        }
    }

    @PreDestroy
    public void preDestroy() {
        try {
            connection.close();
        } catch (IOException e) {
            LOGGER.error("close hbase connection error:", e);
        }
    }

    public Table getOrAddTable(String tableName) throws IOException {
        Boolean exist = tableExist.get(tableName);
        if (!Boolean.TRUE.equals(exist)) {
            synchronized (lock) {
                exist = tableExist.get(tableName);
                if (!Boolean.TRUE.equals(exist)) {
                    try (Admin admin = connection.getAdmin()) {
                        TableName tName = TableName.valueOf(tableName);
                        if (admin.tableExists(tName)) {
                            //if table disable, first delete, then create new table
                            if (admin.isTableDisabled(tName)) {
                                LOGGER.warn("{} table is disable, delete it first.", tableName);
                                admin.deleteTable(tName);
                                createTable(admin, tName);
                                LOGGER.warn("{} table is disable, re-create success.", tableName);
                            }
                        } else {
                            createTable(admin, tName);
                        }
                    } catch (Exception e) {
                        LOGGER.error("create table {} error", tableName, e);
                    } finally {
                        tableExist.put(tableName, Boolean.TRUE);
                    }
                }
            }
        }
        return getHTable(tableName);
    }

    public Table getHTable(String tableName) throws IOException {
        if (null == connection || connection.isClosed()) {
            synchronized (tableCache) {
                if (null == connection || connection.isClosed()) {
                    try {
                        connection = ConnectionFactory.createConnection(configuration);
                    } catch (IOException e) {
                        throw new RuntimeException("openConnection error");
                    }
                    for (ThreadLocal<Table> threadLocal : tableCache.asMap().values()) {
                        try {
                            Table table = threadLocal.get();
                            if (table != null) {
                                table.close();
                            }
                        } catch (Throwable e) {
                            LOGGER.error("close table {} error", tableName, e);
                        } finally {
                            threadLocal.set(null);
                        }
                    }
                }
            }
        }
        ThreadLocal<Table> threadLocal = tableCache.getIfPresent(tableName);
        if (threadLocal == null) {
            synchronized (tableCache) {
                threadLocal = tableCache.getIfPresent(tableName);
                if (threadLocal == null) {
                    threadLocal = new ThreadLocal<>();
                    tableCache.put(tableName, threadLocal);
                }
            }
        }
        Table hTableInterface = threadLocal.get();
        if (hTableInterface == null) {
            hTableInterface = connection.getTable(TableName.valueOf(tableName));
            threadLocal.set(hTableInterface);
        }
        return hTableInterface;
    }

    public void deleteTable(String tableName) throws IOException {
        synchronized (lock) {
            try (Admin admin = connection.getAdmin()) {
                tableExist.remove(tableName);
                TableName tName = TableName.valueOf(tableName);
                if (admin.tableExists(tName)) {
                    if (!admin.isTableDisabled(TableName.valueOf(tableName))) {
                        admin.disableTable(tName);
                    }
                    admin.deleteTable(tName);
                    LOGGER.warn("{} table is deleted.", tableName);
                }
            }
        }
    }

    public void createTable(Admin admin, TableName tableName) throws IOException {
        List<byte[]> columnFamily = createDefaultColumnFamily();

        PartitionRowKeyManager rowKeyManager = new PartitionRowKeyManager();

        byte[][] splitKeys = rowKeyManager.calcSplitKeys();

        HTableDescriptor hTableDescriptor = new HTableDescriptor(tableName);
        for (byte[] columnName : columnFamily) {
            HColumnDescriptor columnDescriptor = new HColumnDescriptor(columnName);
            columnDescriptor.setMaxVersions(1);
            columnDescriptor.setBloomFilterType(BloomType.ROW);

            // todo: 由于local镜像版本的HBASE 会报错： java.lang.RuntimeException: native snappy library not available: this
            //  version of libhadoop was built without snappy support.
            //  先注释掉
            //columnDescriptor.setCompressionType(Compression.Algorithm.SNAPPY);

            columnDescriptor.setDataBlockEncoding(DataBlockEncoding.DIFF);

            hTableDescriptor.addFamily(columnDescriptor);
        }
        admin.createTable(hTableDescriptor, splitKeys);
    }

    public Table getTable(String logicTableName, int day) throws IOException {
        return getHTable(getTableName(logicTableName, day));
    }

    public String getTableName(String logicTableName, int day) {
        return logicTableName.concat("_").concat(Integer.toString(day));
    }

    public List<byte[]> createDefaultColumnFamily() {
        List<byte[]> columns = newArrayList();
        byte[] familyName = "t".getBytes();
        columns.add(familyName);
        return columns;
    }

    public short getShardId(long timestamp, int hashcode) {
        try {
            short regionNum;
            if (stackRegions.isEmpty()) {
                regionNum = defaultRegionNum;
            } else {
                regionNum = getShardNum(timestamp);
            }
            return (short)(Math.abs(hashcode) % regionNum);
        } catch (Exception e) {
            return 0;
        }
    }

    public short getShardNum(long timestamp) {
        Map.Entry<Long, Short> entry = stackRegions.floorEntry(timestamp);
        if (null == entry) {
            return defaultRegionNum;
        }
        return entry.getValue();
    }

    public short getConsistentShardId(int hashcode) {
        return (short)Hashing.consistentHash(Math.abs(hashcode), defaultRegionNum);
    }

    public short getShardId(int hashcode) {
        return (short)(Math.abs(hashcode) % defaultRegionNum);
    }

    void closeCurrentThreadHTable(String tableName) {
        ThreadLocal<Table> threadLocal = tableCache.getIfPresent(tableName);
        if (threadLocal != null) {
            Table currentLocalTable = threadLocal.get();
            if (currentLocalTable != null) {
                try {
                    currentLocalTable.close();
                } catch (IOException e) {
                    LOGGER.error("close table {} error ", tableName, e);
                }
                threadLocal.set(null);
            }
        }
    }

    public void closeHTable(Table table) {
        if (table != null) {
            try {
                table.close();
            } catch (IOException e) {
                LOGGER.warn("Close hbase table error.", e);
            }
        }
    }

    void closeResultScanner(ResultScanner resultScanner) {
        if (resultScanner != null) {
            resultScanner.close();
        }
    }

    public void closeResource(Table table, ResultScanner resultScanner) {
        closeHTable(table);
        closeResultScanner(resultScanner);
    }

}
