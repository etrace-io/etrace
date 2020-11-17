package io.etrace.api.service;

import com.google.common.base.Strings;
import io.etrace.agent.Trace;
import io.etrace.api.config.AsyncConfig;
import io.etrace.api.metric.MetricDatasourceManager;
import io.etrace.common.datasource.MetricQLBean;
import io.etrace.common.datasource.MetricResultSet;
import io.etrace.common.message.metric.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.CountDownLatch;

import static io.etrace.api.service.MetricService.QUERY_TIME_OUT;

@Service
public class MetricAsyncQueryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricAsyncQueryService.class);

    @Autowired
    private MetricDatasourceManager metricDatasourceManager;

    @Async(value = AsyncConfig.THREAD_POOL_TASK_EXECUTOR)
    public void queryData(DeferredResult<MetricResultSet> resultSet, MetricQLBean qlBean, CountDownLatch latch,
                          long beginTime) throws Exception {
        String code = qlBean.getCode();
        boolean waitingTimeOut = checkWaitingTimeOut(beginTime, "data", resultSet, qlBean);
        if (waitingTimeOut) {
            latch.countDown();
            return;
        }
        if (!Strings.isNullOrEmpty(code)) {
            metricDatasourceManager.query(code, resultSet, qlBean, latch);
        } else {
            latch.countDown();
            LOGGER.error("code is empty,can not find lindb connect");
        }
    }

    @Async(value = AsyncConfig.THREAD_POOL_TASK_EXECUTOR)
    public void querySuggestData(DeferredResult<MetricResultSet> resultSet, MetricQLBean qlBean, CountDownLatch latch,
                                 long beginTime) throws Exception {
        String code = qlBean.getCode();
        boolean waitingTimeOut = checkWaitingTimeOut(beginTime, "meta", resultSet, qlBean);
        if (waitingTimeOut) {
            latch.countDown();
            return;
        }
        if (!Strings.isNullOrEmpty(code)) {
            metricDatasourceManager.query(code, resultSet, qlBean, latch);
        } else {
            latch.countDown();
            LOGGER.error("code is empty,can not find lindb connect");
        }
    }

    private boolean checkWaitingTimeOut(long beginTime, String dataType, DeferredResult<MetricResultSet> resultSet,
                                        MetricQLBean qlBean) {
        // check the query is waiting too long
        boolean outOfTime = false;
        long cost = System.currentTimeMillis() - beginTime;
        int costSeconds = Math.round(cost / 1000);
        // 如果等待时间过长，直接为0
        if (cost > QUERY_TIME_OUT) {
            LOGGER.error("the query waiting time [{}] is too long, MetricQLBean: %s", cost, qlBean);
            costSeconds = 11;
            outOfTime = true;
        }
        Counter counter = Trace.newCounter("lindb.thread.waiting.time");
        counter.addTag("db", qlBean.getCode()).addTag("dataType", dataType).addTag("waitingTime",
            String.valueOf(costSeconds));
        counter.once();
        if (outOfTime) {
            MetricResultSet metricResultSet = new MetricResultSet();
            metricResultSet.setErrorMsg("waiting thread too long！");
            metricResultSet.setName(qlBean.getName());
            resultSet.setResult(metricResultSet);
            //查询超时，让线程返回，不再等待
        }

        return outOfTime;
    }

}
