package io.etrace.plugins.prometheus.pushgateway.model;

import java.util.List;

/**
 * Prometheus PushGateway data model
 */
public class PrometheusMetricV1 {


    public final String name;
    public final PrometheusTypeEnumV1 type;
    public final String help;
    public final List<PrometheusMetricSampleV1> samples;

    public PrometheusMetricV1(String name, PrometheusTypeEnumV1 type, String help, List<PrometheusMetricSampleV1> samples) {
        this.name = name;
        this.type = type;
        this.help = help;
        this.samples = samples;
    }
}
