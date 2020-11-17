package io.etrace.api.util;

import io.etrace.api.model.Target;
import io.etrace.common.datasource.MetricBean;
import io.etrace.common.datasource.TagFilter;
import io.etrace.common.util.JSONUtil;

import java.io.IOException;
import java.util.List;

public class MetricBeanUtil {

    public static MetricBean convert(Target target) {
        MetricBean metricBean = new MetricBean();
        metricBean.setEntity(target.getEntity());
        metricBean.setPrefix(target.getPrefix());
        metricBean.setMeasurement(target.getMeasurement());
        List<String> fields = (List<String>)target.getFields();
        metricBean.setFields(fields);
        metricBean.setGroupBy((List<String>)target.getGroupBy());
        Object object = target.getTagFilters();
        if (object != null) {
            try {
                metricBean.setTagFilters(JSONUtil.toArray(JSONUtil.toString(object), TagFilter.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        metricBean.setFunctions(target.getFunctions());
        metricBean.setFrom(target.getFrom());
        metricBean.setTo(target.getTo());
        metricBean.setOrderBy(target.getOrderBy());
        return metricBean;
    }
}
