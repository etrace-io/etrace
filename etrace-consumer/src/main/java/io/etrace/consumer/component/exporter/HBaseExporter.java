package io.etrace.consumer.component.exporter;

import com.google.common.collect.Lists;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.Exporter;
import io.etrace.common.pipeline.impl.DefaultAsyncTask;
import io.etrace.consumer.config.ConsumerProperties;
import io.etrace.consumer.storage.hbase.IHBaseClient;
import io.etrace.consumer.storage.hbase.IHBaseTableNameFactory;
import io.etrace.consumer.storage.hbase.impl.DaySharding;
import io.etrace.consumer.task.CleanHBaseAndHDFSTask;
import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HBaseExporter extends DefaultAsyncTask implements Exporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HBaseExporter.class);

    @Autowired
    public IHBaseClient client;

    @Autowired
    private IHBaseTableNameFactory ihBaseTableNameFactory;

    @Autowired
    private CleanHBaseAndHDFSTask cleanHBaseAndHDFSTask;

    @Autowired
    public ConsumerProperties consumerProperties;

    private Map<Integer, List<Put>> putMapToFlush = new HashMap<>();

    private String tableNamePrefix;
    /**
     * stack/metric 等逻辑上的 table名字，与 TableSchema.getLogicalTableName() 一致。
     */
    private String logicTable;
    @Autowired
    private DaySharding timeSharding;

    private int flushThreshold;
    private int daysToCleanTable;

    public HBaseExporter(String name, Component component, Map<String, Object> params) {
        super(name, component, params);
        String tableName = String.valueOf(params.get("tableName"));
        if (tableName == null) {
            LOGGER.error("HBaseExporter initiation failed. Component [{}] requires the mandatory parameter "
                    + "'tableName'. Review your pipeline configuration file. System.exit()",
                component.getName());
            System.exit(1);
        } else {
            this.tableNamePrefix = tableName;
        }

        String logicTable = String.valueOf(params.get("logicalTable"));
        if (logicTable != null) {
            this.logicTable = logicTable;
        } else {
            this.logicTable = tableName;
        }

        if (params.containsKey("daysToCleanTable")) {
            daysToCleanTable = Integer.parseInt(String.valueOf(params.get("daysToCleanTable")));
        }

        this.flushThreshold = Integer.parseInt(String.valueOf(params.get("flushSize")));
    }

    @PostConstruct
    public void postConstruct() {
        ihBaseTableNameFactory.registerLogicTableNameToTableNameMapping(logicTable, tableNamePrefix);
        if (daysToCleanTable == 0) {
            daysToCleanTable = consumerProperties.getKeeper();
        }
        cleanHBaseAndHDFSTask.registerTableToDelete(tableNamePrefix, daysToCleanTable);
    }

    @Override
    public void startup() {
        super.startup();
    }

    @Override
    public void onTimeTick() {
        flushAll();
        super.onTimeTick();
    }

    @Override
    public void processEvent(Object key, Object event) throws Exception {
        Put put = (Put)event;
        int day = timeSharding.sharding(put.getTimeStamp());

        List<Put> putListToFlush = putMapToFlush.compute(day, (k, oldValue) -> {
            if (oldValue == null) {
                return Lists.newArrayList(put);
            } else {
                oldValue.add(put);
                return oldValue;
            }
        });
        //flush
        if (putListToFlush.size() >= flushThreshold) {
            flushAndClean(day, putListToFlush);
        }
    }

    private void flushAndClean(int day, List<Put> puts) {
        writeHBase(day, puts);
        puts.clear();
    }

    public void flushAll() {
        putMapToFlush.forEach(this::flushAndClean);
        putMapToFlush.clear();
    }

    private void writeHBase(int day, List<Put> puts) {
        boolean result = client.executeBatch(logicTable,
            ihBaseTableNameFactory.getPhysicalTableNameByTableNamePrefix(tableNamePrefix, day), puts);
    }
}
