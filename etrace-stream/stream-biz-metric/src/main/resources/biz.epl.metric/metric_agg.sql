module biz.epl.metric.metric_agg;

import io.etrace.stream.biz.metric.event.MetricWithHeader;

create schema metricWithHeader as MetricWithHeader;

@Name("metrics")
@Metric(name="{name}",tags={"tags"},fields={"fields"},sampling="sampling",source="source")
select mh.metric.source                   as source,
       mh.metric.metricName               as name,
       mh.metric.tags                     as tags,
       fields_agg(mh.metric.fields)       as fields,
       metricSampling(mh.metric)          as sampling,
       trunc_sec(mh.metric.timestamp, 10) as timestamp
from metricWithHeader as mh
group by metric_key(mh.metric.metricName, mh.metric.source, mh.metric.tags), trunc_sec(mh.metric.timestamp, 10);
