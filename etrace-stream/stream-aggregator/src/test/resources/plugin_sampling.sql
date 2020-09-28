module plugin.sampling;

@ Name ("sampling")
@Metric(name = "gauge", sampling = "gaugeMsg", fields = {"gauge"})
@Metric(name = "counter", sampling = "counterMsg", fields = {"gauge"})
@Metric(name = "timer", sampling = "timerMsg", fields = {"gauge"})
@Metric(name = "payload", sampling = "payloadMsg", fields = {"gauge"})
@Metric(name = "histogram", sampling = "histogramMsg", fields = {"gauge"})
select sampling("Gauge", time, name)      as gaugeMsg,
       sampling("Counter", name)          as counterMsg,
       sampling("Timer", value, name)     as timerMsg,
       sampling("Payload", value, name)   as payloadMsg,
       sampling("Histogram", value, name) as histogramMsg,
       f_gauge(gauge(time, value))        as gauge,
       trunc_sec(time, 30)
from mock_event
group by trunc_sec(time, 30);
