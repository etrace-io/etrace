package io.etrace.consumer.storage.hbase;

/**
 * HBASE Table有三个"名字"：
 * 1. logicalName: 如trace/metric等，与TableSchema.getLogicalTableName()的名字一致。
 * 2. tableNamePrefix: 可通过pipeline配置的物理表名前缀。其与logicalName有一一对应的关系。
 * 3. physicalTableName: 根据数据的timestamp生成的具体物理表名，以tableNamePrefix为前缀。
 */
public interface IHBaseTableNameFactory {

    /**
     * 供其他组件注册 logicTableName -> tableNamePrefix 的映射关系；
     * @param logicTableName
     * @param tableNamePrefix
     */
    void registerLogicTableNameToTableNameMapping(String logicTableName, String tableNamePrefix);

    /**
     * 以配置的 table名字生成 物理的表名
     */
    String getPhysicalTableNameByTableNamePrefix(String tableNamePrefix, int day);

    /**
     * 以逻辑的table名字生成 物理的表名
     */
    String getPhysicalTableNameByLogicalTableName(String logicalTableName, int day);

}
