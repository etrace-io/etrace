package io.etrace.api.metric;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.etrace.api.model.po.ui.MetricDataSource;
import io.etrace.api.model.po.ui.MonitorEntity;
import io.etrace.api.service.DataSourceService;
import io.etrace.api.service.MonitorEntityService;
import io.etrace.common.constant.Status;
import io.etrace.common.datasource.MetricBean;
import io.etrace.common.datasource.MetricDatasourceService;
import io.etrace.common.datasource.MetricQLBean;
import io.etrace.common.datasource.MetricResultSet;
import io.etrace.common.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MetricDatasourceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetricDatasourceManager.class);

    /**
     * key datasource Id,value datasource
     */
    private final Map<Long, MetricDataSource> dataSourceMap = new HashMap<>();

    private Map<String, MonitorEntity> monitorEntityCodeMap = new HashMap<>();

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private MetricDatasourceService metricDatasourceService;

    @Autowired
    private MonitorEntityService monitorEntityService;

    public String generateSuggestQL(MetricBean bean) {
        return metricDatasourceService.generateSuggestQL(bean);
    }

    public List<MetricQLBean> generateQLBean(MetricBean bean, Date date) throws Exception {
        return metricDatasourceService.generateQLBean(bean, date);
    }

    private void getLindbConnectInfoByDataSourceId(Long datasourceId) throws Exception {
        if (!metricDatasourceService.connected(datasourceId)) {
            MetricDataSource dataSource = findMetricDatasourceConfig(datasourceId);
            dataSourceMap.put(datasourceId, dataSource);

            metricDatasourceService.doConnect(datasourceId, dataSource.getName(), dataSource.getConfig());
        }
    }

    private MetricDataSource findMetricDatasourceConfig(Long datasourceId) throws Exception {
        MetricDataSource dataSource = dataSourceMap.get(datasourceId);
        if (null == dataSource) {
            Optional<MetricDataSource> op = dataSourceService.findById(datasourceId);
            if (op.isPresent()) {
                dataSource = op.get();
                if (Status.Active.equals(dataSource.getStatus())) {
                    dataSourceMap.put(datasourceId, dataSource);
                    return dataSource;
                }
            }
            throw new Exception("could not find datasource,sourceId:" + datasourceId);
        }
        return dataSource;
    }

    /**
     * reload lindb connect cache ,
     */
    @Scheduled(initialDelay = 1000, fixedRate = 5 * 60 * 1000)
    public synchronized void reload() throws IOException {
        Iterable<MonitorEntity> monitorEntityList = monitorEntityService.findAll();
        Iterable<MetricDataSource> dataSourceList = dataSourceService.findAll();
        reloadMonitorEntity(monitorEntityList);
        reloadDatasource(dataSourceList);
    }

    public synchronized void reloadMonitorEntity(Iterable<MonitorEntity> monitorEntityList) {
        // clean the lindb
        Collection<MonitorEntity> monitorEntities = monitorEntityCodeMap.values();
        if (!CollectionUtils.isEmpty(monitorEntities)) {
            cleanLinDB(monitorEntityList, Lists.newArrayList(monitorEntities));
        }

        Map<String, MonitorEntity> entityCodeMap = new HashMap<>();
        //check if need to change the database
        monitorEntityList.forEach(monitorEntity -> {
            if (!Strings.isNullOrEmpty(monitorEntity.getDatabase()) && null != monitorEntity.getDatasourceId()) {
                entityCodeMap.put(monitorEntity.getDatabase(), monitorEntity);
            }
            //replace the old map
            monitorEntityCodeMap = entityCodeMap;
        });
    }

    public synchronized void reloadDatasource(Iterable<MetricDataSource> dataSourceList) throws IOException {
        Map<Long, MetricDataSource> newDataSourceMap = new HashMap();
        dataSourceList.forEach(dataSource -> {
            if (Status.Active.equals(dataSource.getStatus())) {
                newDataSourceMap.put(dataSource.getId(), dataSource);
            }
        });
        // check old connection and build new connect
        Map<Long, MetricDataSource> updateDataSourceMap = new HashMap<>();

        Iterator<Map.Entry<Long, MetricDataSource>> it = dataSourceMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, MetricDataSource> entry = it.next();
            Long datasourceId = entry.getKey();
            MetricDataSource oldDataSource = entry.getValue();

            MetricDataSource dataSource = newDataSourceMap.get(datasourceId);
            //if DataSource is delete or is inactive ,delete it from cache
            if (dataSource == null) {
                metricDatasourceService.closeConnection(datasourceId);
                it.remove();
                // if the cache is update,close old connect,and build new connect
            } else if (dataSource.getUpdatedAt().compareTo(oldDataSource.getUpdatedAt()) > 0) {
                updateDataSourceMap.put(datasourceId, dataSource);

                metricDatasourceService.closeConnection(datasourceId);
                metricDatasourceService.doConnect(datasourceId, dataSource.getName(), dataSource.getConfig());
            }
        }

        updateDataSourceMap.forEach((datasourceId, dataSource) -> {
            dataSourceMap.put(datasourceId, dataSource);
        });

        // add new connection
        for (Map.Entry<Long, MetricDataSource> entry : newDataSourceMap.entrySet()) {
            Long dataSourceId = entry.getKey();
            MetricDataSource dataSource = entry.getValue();
            if (!metricDatasourceService.connected(dataSourceId)) {
                dataSourceMap.put(dataSourceId, dataSource);
                metricDatasourceService.doConnect(dataSourceId, dataSource.getName(), dataSource.getConfig());
            }
        }
        metricDatasourceService.checkConnectionAndRebuildConnection();
    }

    public void query(String code, DeferredResult<MetricResultSet> resultSet, MetricQLBean qlBean, CountDownLatch latch)
        throws Exception {
        MonitorEntity monitorEntity = monitorEntityCodeMap.getOrDefault(code,
            monitorEntityService.findByCode(code).orElse(null));

        if (null == monitorEntity || Strings.isNullOrEmpty(monitorEntity.getDatabase()) || null == monitorEntity
            .getDatasourceId()) {
            throw new RuntimeException(
                "find db info fail ,code：" + code + "  , monitor entity:" + JSONUtil.toString(monitorEntity));
        }
        getLindbConnectInfoByDataSourceId(monitorEntity.getDatasourceId());
        metricDatasourceService.sq(monitorEntity.getDatasourceId(), monitorEntity.getDatabase(), false, resultSet,
            qlBean, latch);
    }

    private void cleanLinDB(Iterable<MonitorEntity> monitorEntityList, List<MonitorEntity> oldMonitorEntityList) {
        if (CollectionUtils.isEmpty(oldMonitorEntityList)) {
            return;
        }
        Map<Long, MonitorEntity> monitorEntityMap = new HashMap<>();
        monitorEntityList.forEach(monitorEntity -> {
            monitorEntityMap.put(monitorEntity.getId(), monitorEntity);
        });
        oldMonitorEntityList.forEach(oldMonitorEntity -> {
            MonitorEntity monitorEntity = monitorEntityMap.get(oldMonitorEntity.getId());
            if (null == oldMonitorEntity.getDatasourceId() || Strings.isNullOrEmpty(oldMonitorEntity.getDatabase())) {
                return;
            }
            // if the code or datasource id or databasename changes ,clean the cache
            if (null == monitorEntity || !oldMonitorEntity.getDatasourceId().equals(monitorEntity.getDatasourceId()) ||
                !oldMonitorEntity.getCode().equals(monitorEntity.getCode())
                || !oldMonitorEntity.getDatabase().equals(monitorEntity.getDatabase())) {
                metricDatasourceService.cleanConnectionCache(oldMonitorEntity.getDatasourceId(),
                    oldMonitorEntity.getDatabase());
            }
        });
    }
}
