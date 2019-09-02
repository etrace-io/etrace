package io.etrace.common.modal.metric;

public interface Histogram extends Metric<Histogram> {
    void record(long amount);
}
