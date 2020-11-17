package io.etrace.consumer.component.exporter;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.carrotsearch.hppc.cursors.ShortObjectCursor;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.Exporter;
import io.etrace.common.pipeline.TimeTick;
import io.etrace.common.pipeline.impl.DefaultAsyncTask;
import io.etrace.consumer.storage.hbase.HBaseClient;
import io.etrace.consumer.storage.hbase.impl.DaySharding;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HBaseExporter extends DefaultAsyncTask implements Exporter {

    @Autowired
    public HBaseClient client;
    private IntObjectMap<ShortObjectMap<List<Put>>> putMap = new IntObjectHashMap<>(32);
    private IntObjectHashMap<ShortLongHashMap> regionFlushTime = new IntObjectHashMap<>(32);
    private String logicTable;
    @Autowired
    private DaySharding timeSharding;
    private int flushInterval = 6000;
    private int flushThreshold;

    public HBaseExporter(String name, Component component, Map<String, Object> params) {
        super(name, component, params);
        String tableName = (String)params.get("tableName");
        if (tableName == null) {
            this.logicTable = name;
        } else {
            this.logicTable = tableName;
        }

        this.flushThreshold = Integer.parseInt(params.get("flushSize").toString());
    }

    @Override
    public void startup() {
        super.startup();
    }

    @Override
    public void processEvent(Object key, Object event) throws Exception {
        if (key instanceof TimeTick) {
            flushIfNeed();
            return;
        }
        Put put = (Put)event;
        int day = timeSharding.sharding(put.getTimeStamp());
        short shard = (short)key;

        ShortObjectMap<List<Put>> dayMap = putMap.get(day);
        if (null == dayMap) {
            dayMap = new ShortObjectHashMap<>(64);
            putMap.put(day, dayMap);
        }

        List<Put> putList = dayMap.get(shard);
        if (null == putList) {
            putList = new ArrayList<>(flushThreshold);
            dayMap.put(shard, putList);
        }
        putList.add(put);
        //flush
        if (putList.size() >= flushThreshold) {

            flush(day, shard, new ArrayList<>(putList));
            putList.clear();
            updateFlushTime(day, shard);
        }
    }

    private void flush(int day, short regionId, List<Put> puts) {
        writeHBase(day, regionId, puts);
    }

    public void flushIfNeed() {
        long now = System.currentTimeMillis();

        for (IntObjectCursor<ShortObjectMap<List<Put>>> tableEntry : putMap) {
            int day = tableEntry.key;
            ShortObjectMap<List<Put>> regionMap = tableEntry.value;

            if (regionMap.size() > 0) {
                for (ShortObjectCursor<List<Put>> regionEntry : regionMap) {
                    short regionId = regionEntry.key;
                    List<Put> puts = regionEntry.value;
                    if (puts.size() > 0 && (now - getLastFlushTime(day, regionId)) > flushInterval) {
                        flush(day, regionId, new ArrayList<>(puts));
                    }
                    regionEntry.value.clear();
                    updateFlushTime(day, regionId);
                }
            }
        }
    }

    private void writeHBase(int day, short regionId, List<Put> puts) {
        updateFlushTime(day, regionId);

        client.executeBatch(logicTable, day, puts);
    }

    private void updateFlushTime(int tableId, short regionId) {
        ShortLongHashMap tableMap = regionFlushTime.get(tableId);
        if (null == tableMap) {
            tableMap = new ShortLongHashMap(64);
            regionFlushTime.put(tableId, tableMap);
        }
        tableMap.put(regionId, System.currentTimeMillis());
    }

    private long getLastFlushTime(int tableId, short regionId) {
        ShortLongHashMap tableMap = regionFlushTime.get(tableId);
        if (null == tableMap) {
            tableMap = new ShortLongHashMap(64);
            regionFlushTime.put(tableId, tableMap);
        }
        long lastTime = tableMap.getOrDefault(regionId, 0);
        if (lastTime < 1) {
            tableMap.put(regionId, System.currentTimeMillis());
        }
        return tableMap.getOrDefault(regionId, 0);
    }
}
