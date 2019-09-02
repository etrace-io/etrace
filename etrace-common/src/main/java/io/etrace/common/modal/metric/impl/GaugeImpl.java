package io.etrace.common.modal.metric.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import io.etrace.common.message.MetricManager;
import io.etrace.common.modal.metric.AbstractMetric;
import io.etrace.common.modal.metric.Gauge;
import io.etrace.common.modal.metric.Metric;
import io.etrace.common.modal.metric.MetricType;

import java.io.IOException;

public class GaugeImpl extends AbstractMetric<Gauge> implements Gauge {
    private double value;

    public GaugeImpl(MetricManager metricManager, String name) {
        super(metricManager, name);
    }

    /**
     * 无法使用类似如下代码：
     * <p>
     * com.alibaba.metrics.MetricManager.register("application", MetricName.build(getName()).tagged(tags),
     * (com.alibaba.metrics.Gauge<Double>)() -> value);
     * <p>
     * <p>
     * 因为会导致重复注册，最终报错IllegalArgumentException。
     * <p>
     * 使用try-catch的方式，会影响应用的性能。
     * <p>
     * 因此对于"GAUGE"不做兼容。
     *
     * @param value the last value
     */
    @Override
    public void value(double value) {
        if (!tryCompleted()) {
            return;
        }
        this.value = value;
        if (manager != null) {
            manager.addMetric(this);
        }
    }

    public double getValue() {
        return value;
    }

    @Override
    public MetricType getMetricType() {
        return MetricType.Gauge;
    }

    @Override
    public void merge(Metric metric) {
        if (metric instanceof GaugeImpl) {
            GaugeImpl gauge = (GaugeImpl)metric;
            if (gauge.timestamp > timestamp) {
                setTimestamp(gauge.timestamp);
                value = gauge.value;
            }
        }
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
        super.write(generator);
        generator.writeNumber(value);
    }
}
