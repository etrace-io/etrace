package io.etrace.api.service;

import io.etrace.api.exception.BadRequestException;
import io.etrace.api.model.bo.GroupResult;
import io.etrace.api.model.bo.SelectResult;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.common.datasource.MetricBean;
import io.etrace.common.datasource.MetricResultSet;
import io.etrace.common.datasource.TagFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class AppMetricService {

    private final String appIdChangeMeasurement = "arch.holmes.change";
    private final String appIdAlertMeasurement = "arch.holmes.alert";
    @Autowired
    private MetricService metricService;

    public int[] queryApp(String appId, String from, String to, ETraceUser user) throws BadRequestException {
        List<MetricBean> metricBeanList = new ArrayList<>();
        metricBeanList.add(getAppMetricBean(appIdChangeMeasurement, appId, from, to));
        metricBeanList.add(getAppMetricBean(appIdAlertMeasurement, appId, from, to));
        List<MetricResultSet> resultSets = metricService.queryDataWithMetricBean(metricBeanList, user);
        int[] ret = new int[2];
        for (int i = 0, len = resultSets.size(); i < len; i++) {
            MetricResultSet resultSet = resultSets.get(i);
            if (resultSet != null) {
                SelectResult selectResult = (SelectResult)resultSet.getResults();
                List<GroupResult> results = selectResult.getGroups();
                if (results != null && results.size() > 0) {
                    List<Number> numbers = results.get(0).getFields().get("t_sum(count)");
                    if (numbers != null && numbers.size() > 0) {
                        ret[i] = numbers.get(0).intValue();
                    }
                }
            }
        }
        return ret;
    }

    private MetricBean getAppMetricBean(String measurement, String appId, String from, String to) {
        MetricBean metricBean = new MetricBean();
        metricBean.setEntity("app_metric");
        metricBean.setMeasurement(measurement);
        metricBean.setFields(Arrays.asList("t_sum(count)"));
        metricBean.setFrom(from);
        metricBean.setTo(to);
        TagFilter tagFilter = new TagFilter();
        tagFilter.setOp("=");
        tagFilter.setKey("appId");
        tagFilter.setValue(Arrays.asList(appId));
        metricBean.setTagFilters(Arrays.asList(tagFilter));
        return metricBean;
    }
}
