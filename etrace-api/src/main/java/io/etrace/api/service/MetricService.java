package io.etrace.api.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.etrace.agent.Trace;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.common.constant.Constants;
import io.etrace.common.datasource.MetricBean;
import io.etrace.common.datasource.MetricDatasourceService;
import io.etrace.common.datasource.MetricQLBean;
import io.etrace.common.datasource.MetricResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class MetricService {

    public static final Long QUERY_TIME_OUT = TimeUnit.SECONDS.toMillis(10);
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricService.class);
    @Autowired
    private MetricAsyncQueryService metricAsyncQueryService;
    @Autowired
    private MetricDatasourceService metricDatasourceService;
    private Set<String> blackQlMap = new HashSet<>(1000);

    private Set<String> blackMeasurementSet = new HashSet<>(100);

    public MetricResultSet querySuggest(MetricQLBean qlBean, ETraceUser user) throws Exception {
        logQueryUser("meta", qlBean.getCode(), user);
        CountDownLatch latch = new CountDownLatch(1);
        DeferredResult<MetricResultSet> result = new DeferredResult<>(QUERY_TIME_OUT);
        metricAsyncQueryService.querySuggestData(result, qlBean, latch, System.currentTimeMillis());
        boolean await = latch.await(QUERY_TIME_OUT, TimeUnit.MILLISECONDS);
        checkQueryTimeOut(await, "meta", qlBean.getCode(), qlBean.getQl());

        MetricResultSet metricResultSet = (MetricResultSet)result.getResult();
        checkAndLogNoResult(metricResultSet, qlBean.getQl());

        return metricResultSet;
    }

    /**
     * 跟下面方法相似，合成一个
     *
     * @param qlBean
     * @return
     * @throws Throwable
     */
    @Deprecated
    public List<MetricResultSet> queryMetric(MetricQLBean qlBean, ETraceUser user) throws Throwable {
        logQueryUser("data", qlBean.getCode(), user);
        List<MetricResultSet> resultSets = Lists.newLinkedList();
        CountDownLatch latch = new CountDownLatch(1);
        DeferredResult<MetricResultSet> result = new DeferredResult<>(QUERY_TIME_OUT);
        metricAsyncQueryService.queryData(result, qlBean, latch, System.currentTimeMillis());

        boolean await = latch.await(QUERY_TIME_OUT, TimeUnit.MILLISECONDS);
        checkQueryTimeOut(await, "data", qlBean.getCode(), qlBean.getQl());
        MetricResultSet metricResultSet = (MetricResultSet)result.getResult();
        checkAndLogNoResult(metricResultSet, qlBean.getQl());
        resultSets.add(metricResultSet);
        return resultSets;
    }

    public List<MetricResultSet> queryMetrics(List<MetricQLBean> qlsBean, ETraceUser user) throws Throwable {
        List<MetricResultSet> resultSets = Lists.newLinkedList();
        List<DeferredResult<MetricResultSet>> futures = Lists.newArrayList();
        int size = qlsBean.size();
        CountDownLatch latch = new CountDownLatch(size);
        for (MetricQLBean qlBean : qlsBean) {
            logQueryUser("data", qlBean.getCode(), user);
            DeferredResult<MetricResultSet> defer = new DeferredResult<>(QUERY_TIME_OUT);
            metricAsyncQueryService.queryData(defer, qlBean, latch, System.currentTimeMillis());
            futures.add(defer);
        }
        boolean await = latch.await(QUERY_TIME_OUT, TimeUnit.MILLISECONDS);
        for (int i = 0; i < size; i++) {
            checkQueryTimeOut(await, "data", qlsBean.get(i).getCode(), qlsBean.get(i).getQl());
            DeferredResult<MetricResultSet> future = futures.get(i);
            MetricResultSet metricResultSet = (MetricResultSet)future.getResult();
            //set metric name
            resultSets.add(metricResultSet);
            checkAndLogNoResult(metricResultSet, qlsBean.get(i).getQl());
        }
        return resultSets;
    }

    private void checkQueryTimeOut(boolean awaitSuccess, String type, String db, String ql) {
        if (!awaitSuccess) {
            throw new RuntimeException(String.format("query too long in db(%s) for ql($%s)", db, ql));
        }
    }

    /**
     * log queries with no results or timeouts
     *
     * @param metricResultSet lindb query result
     * @param ql              lindb query ql
     */
    private void checkAndLogNoResult(MetricResultSet metricResultSet, String ql) {
        if (null == metricResultSet || null == metricResultSet.getResults()) {
            Trace.logEvent("lindbQuery", "no_search_result", Constants.FAILURE);
        } else {
            Trace.logEvent("lindbQuery", "search_result", Constants.SUCCESS);
        }
    }

    private boolean checkIsBlackQl(String ql) {
        return !Strings.isNullOrEmpty(ql) && blackQlMap.contains(ql.trim());
    }

    private void logQueryUser(String dataType, String db, ETraceUser user) {
        Trace.newCounter("lindb.query.userInfo")
            .addTag("user", user.getUsername())
            .addTag("openApi", user.getIsApiUser().toString())
            .addTag("dataType", dataType)
            .once();
        Trace.newCounter("lindb.query.db.user")
            .addTag("user", user.getUsername())
            .addTag("database", db)
            .once();
    }

    /**
     * update  blackQlMap and blackMeasurementSet based on sql blacklist
     */
    @Scheduled(initialDelayString = "PT10S", fixedRateString = "PT5M")
    public synchronized void findLimitSql() {
        LOGGER.info("begin to reload limit sql");
    }

    public List<MetricResultSet> queryDataWithMetricBean(List<MetricBean> metricBeanList, ETraceUser user)
        throws BadRequestException {
        List<MetricQLBean> beans = Lists.newLinkedList();
        Date date = new Date();
        for (MetricBean bean : metricBeanList) {
            if (validation(bean)) {
                try {beans.addAll(metricDatasourceService.generateQLBean(bean, date));} catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                throw new BadRequestException("illegal MetricBean！");
            }
        }
        try {
            return queryDataWithMetricQLBean(beans, user);
        } catch (Throwable throwable) {
            throw new BadRequestException("query metrics error:" + throwable.getMessage());
        }
    }

    public List<MetricResultSet> queryDataWithMetricQLBean(List<MetricQLBean> beans, ETraceUser user)
        throws BadRequestException {
        try {
            //检查有没有黑名单中的sql
            validateQl(beans);
            return queryMetrics(beans, user);
        } catch (Throwable throwable) {
            throw new BadRequestException("query metrics error:" + throwable.getMessage());
        }
    }

    private boolean validation(MetricBean bean) throws BadRequestException {
        if (Strings.isNullOrEmpty(bean.getEntity())
            || Strings.isNullOrEmpty(bean.getMeasurement())
            || bean.getFields() == null || bean.getFields().size() == 0) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        if (org.springframework.util.StringUtils.isEmpty(bean.getPrefix())) {
            sb.append(bean.getMeasurement());
        } else {
            sb.append(bean.getPrefix()).append(".").append(bean.getMeasurement());
        }
        if (blackMeasurementSet.contains(sb.toString())) {
            throw new BadRequestException("the measurement is forbidden");
        }
        return true;

    }

    private void validateQl(List<MetricQLBean> beans) throws BadRequestException {
        for (MetricQLBean qlbean : beans) {
            if (null != qlbean && checkIsBlackQl(qlbean.getBaseQl())) {
                // the ql is limited ，
                throw new BadRequestException("the ql is forbidden");
            }
        }
    }

}
