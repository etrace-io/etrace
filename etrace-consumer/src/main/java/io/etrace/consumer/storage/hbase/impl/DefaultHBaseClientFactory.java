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

package io.etrace.consumer.storage.hbase.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.etrace.common.util.TimeHelper;
import io.etrace.consumer.config.ConsumerProperties;
import io.etrace.consumer.storage.hbase.IHBaseClientFactory;
import io.etrace.consumer.storage.hbase.IHBaseTableNameFactory;
import io.etrace.consumer.storage.hbase.PartitionRowKeyHelper;
import io.etrace.consumer.storage.hbase.TableSchema;
import joptsimple.internal.Strings;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Lazy
@Component
//@ConditionalOnMissingBean(IHBaseClientFactory.class)
public class DefaultHBaseClientFactory implements IHBaseClientFactory {
    private final Logger LOGGER = LoggerFactory.getLogger(DefaultHBaseClientFactory.class);

    public static String KEY_REGION_SIZE = "key_region_size";

    private final short defaultRegionNum = 60;
    private final Object lock = new Object();
    private Map<String, byte[]> columnFamilyByLogicalTableName = Maps.newHashMap();
    @Autowired
    private ConsumerProperties consumerProperties;
    @Autowired
    private List<TableSchema> tableSchemas;
    @Autowired
    private IHBaseTableNameFactory ihBaseTableNameFactory;

    private List<ConsumerProperties.TableAndRegion> tableToRegionMapping = Lists.newArrayList();

    /**
     * 实际的 hbase table（非逻辑的）与其 region数量的映射表
     */
    private Map<String, Integer> tableNameAndRegionSizeMap = Maps.newConcurrentMap();

    private Configuration configuration;
    private Connection connection;
    private Map<String, Boolean> tableExist = new ConcurrentHashMap<>();
    private Cache<String, ThreadLocal<HTable>> tableCache = CacheBuilder.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .expireAfterAccess(30, TimeUnit.MINUTES)
        .removalListener((RemovalListener<String, ThreadLocal<HTable>>)removalNotification -> {
            if (null != removalNotification.getValue() && null != removalNotification.getValue().get()) {
                try {
                    removalNotification.getValue().get().close();
                } catch (Exception e) {
                    LOGGER.error("close hbase table  error,  is {}", removalNotification.getKey(), e);
                }
            }
        }).build();

    @PostConstruct
    public void postConstruct() {
        configuration = new Configuration();
        configuration.addResource("hbase-site-default.xml");
        configuration.addResource("hbase-site.xml");
        try {
            connection = ConnectionFactory.createConnection(configuration);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create HBase pool");
        }

        for (ConsumerProperties.TableAndRegion table : consumerProperties.getHbase()) {
            LOGGER.warn("Logical hbase table [{}] will assign to [{}] regions. Other tables will assign default "
                + "[{}] regions. ", table.getTableName(), table.getRegionSize(), defaultRegionNum);
            tableToRegionMapping.add(
                new ConsumerProperties.TableAndRegion(table.getTableName(), table.getRegionSize()));
        }

        loadAllTableColumnFamily();
    }


    @PreDestroy
    public void preDestroy() {
        try {
            connection.close();
        } catch (IOException e) {
            LOGGER.error("close hbase connection error:", e);
        }
    }

    public void loadAllTableColumnFamily() {
        tableSchemas.forEach(tableSchema -> columnFamilyByLogicalTableName.put(tableSchema.getLogicalTableName(),
            tableSchema.getColumnFamily()));
    }

    @Override
    public List<byte[]> getColumnFamilyByTable(String table) {
        byte[] column = columnFamilyByLogicalTableName.get(table);
        if (column == null) {
            LOGGER.warn("Can't find ColumnFamily setting for table [{}], so take empty list instead. ", table);
            return Collections.emptyList();
        } else {
            return Lists.newArrayList(column);
        }
    }

    @Override
    public HTable getOrCreateTable(String logicTableName, String physicalTableName) throws IOException {
        Boolean exist = tableExist.get(physicalTableName);
        if (!Boolean.TRUE.equals(exist)) {
            synchronized (lock) {
                exist = tableExist.get(physicalTableName);
                if (!Boolean.TRUE.equals(exist)) {
                    try (Admin admin = connection.getAdmin()) {
                        TableName tName = TableName.valueOf(physicalTableName);
                        if (admin.tableExists(tName)) {
                            //if table disable, first delete, then create new table
                            if (admin.isTableDisabled(tName)) {
                                LOGGER.warn("{} table is disable, delete it first.", physicalTableName);
                                admin.deleteTable(tName);
                                createTable(admin, logicTableName, tName);
                                LOGGER.warn("{} table is disable, re-create success.", physicalTableName);
                            }
                        } else {
                            createTable(admin, logicTableName, tName);
                        }
                    } catch (Exception e) {
                        LOGGER.error("create table {} error", physicalTableName, e);
                    } finally {
                        tableExist.put(physicalTableName, Boolean.TRUE);
                    }
                }
            }
        }
        return getTableByPhysicalName(physicalTableName);
    }

    @Override
    public HTable getTableByPhysicalName(String physicalTableName) throws IOException {
        if (null == connection || connection.isClosed()) {
            synchronized (tableCache) {
                if (null == connection || connection.isClosed()) {
                    try {
                        connection = ConnectionFactory.createConnection(configuration);
                    } catch (IOException e) {
                        throw new RuntimeException("openConnection error");
                    }
                    for (ThreadLocal<HTable> threadLocal : tableCache.asMap().values()) {
                        try {
                            Table table = threadLocal.get();
                            if (table != null) {
                                table.close();
                            }
                        } catch (Throwable e) {
                            LOGGER.error("close table {} error", physicalTableName, e);
                        } finally {
                            threadLocal.set(null);
                        }
                    }
                }
            }
        }
        ThreadLocal<HTable> threadLocal = tableCache.getIfPresent(physicalTableName);
        if (threadLocal == null) {
            synchronized (tableCache) {
                threadLocal = tableCache.getIfPresent(physicalTableName);
                if (threadLocal == null) {
                    threadLocal = new ThreadLocal<>();
                    tableCache.put(physicalTableName, threadLocal);
                }
            }
        }
        HTable hTableInterface = threadLocal.get();
        if (hTableInterface == null) {
            hTableInterface = (HTable)connection.getTable(TableName.valueOf(physicalTableName));
            threadLocal.set(hTableInterface);
        }
        return hTableInterface;
    }

    @Override
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

    private int getRegionsSizeFromConfiguration(String tableName) {
        for (ConsumerProperties.TableAndRegion tableAndRegion : tableToRegionMapping) {
            if (tableName.startsWith(tableAndRegion.getTableName())) {
                return tableAndRegion.getRegionSize();
            }
        }
        return defaultRegionNum;
    }

    private void createTable(Admin admin, String logicTableName, TableName tableName) throws IOException {
        int regionSize = getRegionsSizeFromConfiguration(tableName.getNameAsString());

        List<byte[]> columnFamily = getColumnFamilyByTable(logicTableName);

        byte[][] splitKeys = PartitionRowKeyHelper.calcSplitKeys(regionSize);

        HTableDescriptor hTableDescriptor = new HTableDescriptor(tableName);
        hTableDescriptor.setValue(KEY_REGION_SIZE, String.valueOf(regionSize));
        for (byte[] columnName : columnFamily) {
            HColumnDescriptor columnDescriptor = new HColumnDescriptor(columnName);
            columnDescriptor.setMaxVersions(1);
            columnDescriptor.setBloomFilterType(BloomType.ROW);

            // todo: 由于local镜像版本的HBASE 会报错： java.lang.RuntimeException: native snappy library not available: this
            //  version of libhadoop was built without snappy support.
            //columnDescriptor.setCompressionType(Compression.Algorithm.SNAPPY);

            columnDescriptor.setDataBlockEncoding(DataBlockEncoding.DIFF);

            hTableDescriptor.addFamily(columnDescriptor);
        }
        admin.createTable(hTableDescriptor, splitKeys);

        tableNameAndRegionSizeMap.put(tableName.getNameAsString(), regionSize);
    }

    private int getTableRegionSize(String physicalTableName) {
        Integer size = tableNameAndRegionSizeMap.get(physicalTableName);
        if (size == null) {
            try {
                String value = getTableByPhysicalName(physicalTableName).getTableDescriptor().getValue(KEY_REGION_SIZE);
                if (Strings.isNullOrEmpty(value)) {
                    size = getRegionsSizeFromConfiguration(physicalTableName);
                } else {
                    size = Integer.parseInt(value);
                }
            } catch (Exception e) {
                // maybe table not exist
                size = getRegionsSizeFromConfiguration(physicalTableName);
                LOGGER.warn("Table [{}] is not found, calculated Region_Size is [{}].", physicalTableName, size);
            }
            tableNameAndRegionSizeMap.put(physicalTableName, size);
        }
        return size;
    }

    @Override
    public short getShardIdByPhysicalTableName(String physicalTableName, int hashcode) {
        return (short)(Math.abs(hashcode) % getTableRegionSize(physicalTableName));
    }

    @Override
    public short getShardIdByLogicalTableName(String logicalTableName, long timestamp, int hashcode) {
        String physicalTableName =  ihBaseTableNameFactory.getPhysicalTableNameByTableNamePrefix(logicalTableName, TimeHelper.getDay(timestamp));
        return getShardIdByPhysicalTableName(physicalTableName, hashcode);
    }

    @Override
    public void closeCurrentThreadHTable(String tableName) {
        ThreadLocal<HTable> threadLocal = tableCache.getIfPresent(tableName);
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

    @Override
    public void closeHTable(HTable table) {
        if (table != null) {
            try {
                table.close();
            } catch (IOException e) {
                LOGGER.warn("Close hbase table error.", e);
            }
        }
    }

    private void closeResultScanner(ResultScanner resultScanner) {
        if (resultScanner != null) {
            resultScanner.close();
        }
    }

    @Override
    public void closeScanner(ResultScanner resultScanner) {
        closeResultScanner(resultScanner);
    }

    @Override
    public boolean tableExists(TableName tableName) {
        try (Admin admin = connection.getAdmin()) {
            return admin.tableExists(tableName);
        } catch (Exception e) {
            LOGGER.error("tableExists for table [{}]", tableName.getNameAsString(), e);
        }
        return false;
    }
}
