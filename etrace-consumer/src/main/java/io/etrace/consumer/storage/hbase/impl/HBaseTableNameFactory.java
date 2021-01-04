package io.etrace.consumer.storage.hbase.impl;

import com.google.common.collect.Maps;
import io.etrace.consumer.storage.hbase.IHBaseTableNameFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HBaseTableNameFactory implements IHBaseTableNameFactory {
    private final Map<String, String> logicTableNameToTableNamePrefixMapping = Maps.newHashMap();

    @Override
    public void registerLogicTableNameToTableNameMapping(String logicTableName, String tableNamePrefix) {
        logicTableNameToTableNamePrefixMapping.put(logicTableName, tableNamePrefix);
    }

    @Override
    public String getPhysicalTableNameByTableNamePrefix(String tableNamePrefix, int day) {
        return tableNamePrefix.concat("_").concat(Integer.toString(day));
    }

    @Override
    public String getPhysicalTableNameByLogicalTableName(String logicalTableName, int day) {
        return getPhysicalTableNameByTableNamePrefix(logicTableNameToTableNamePrefixMapping.getOrDefault(logicalTableName,
            logicalTableName), day);
    }
}
