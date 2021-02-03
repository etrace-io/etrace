package io.etrace.plugins.prometheus.pushgateway.model;

import io.etrace.common.message.metric.MetricHeaderV1;
import io.etrace.common.message.metric.impl.AbstractMetric;
import io.etrace.common.message.trace.MessageHeader;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EtraceMessage {
    MessageHeader messageHeader;
    MetricHeaderV1 metricHeaderV1;
    List<AbstractMetric> metricList;

    public EtraceMessage(MessageHeader messageHeader, MetricHeaderV1 metricHeaderV1, List<AbstractMetric> metricList) {
        this.messageHeader = messageHeader;
        this.metricHeaderV1 = metricHeaderV1;
        this.metricList = metricList;
    }

}
