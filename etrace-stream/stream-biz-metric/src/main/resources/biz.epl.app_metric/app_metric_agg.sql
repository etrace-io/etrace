module biz.epl.metric.app_metric_agg;

import io.etrace.stream.biz.metric.event.MetricWithHeader;

create schema metric as MetricWithHeader;


@Name("metrics")
@Metric(name="{appId}.{name}",tags={"tags","cluster","ezone"},fields={"fields"})
select
header.appId as appId,
header.ezone as ezone,
header.cluster as cluster,
metric.metricName as name,
metric.tags as tags,
trunc_sec(metric.timestamp,10) as timestamp,
fields_agg(metric.fields) as fields
from metric
group by metric_key(header.appId,header.ezone,header.cluster,metric.metricName,metric.tags),trunc_sec(metric.timestamp,10);