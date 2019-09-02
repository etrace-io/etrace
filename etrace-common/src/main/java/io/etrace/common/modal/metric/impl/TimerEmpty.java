package io.etrace.common.modal.metric.impl;

import io.etrace.common.modal.metric.AbstractEmpty;
import io.etrace.common.modal.metric.MetricType;
import io.etrace.common.modal.metric.Timer;

public class TimerEmpty extends AbstractEmpty<Timer> implements Timer {
    @Override
    public void value(long value) {

    }

    @Override
    public void end() {

    }

    @Override
    public Timer setUpperEnable(boolean upperEnable) {
        return this;
    }

    @Override
    public boolean isUpperEnable() {
        return false;
    }

    @Override
    public MetricType getMetricType() {
        return MetricType.Timer;
    }
}
