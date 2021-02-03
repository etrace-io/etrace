module stream.biz.app.exception;


create schema exception as io.etrace.stream.biz.app.event.Exception;

@ Name ('exception')
@Metric(name = '{appId}.exception', tags = {'appId', 'cluster', 'name', 'type', 'ezone', 'hostName'}, fields = {'count'}, sampling = 'sampling')
@Metric(name = 'etrace.dashboard.exception', tags = {'appId', 'cluster', 'name', 'type', 'ezone'}, fields = {'count'})
select header.ezone                    as ezone,
       header.appId                    as appId,
       header.cluster                  as cluster,
       header.hostName                 as hostName,
       type                            as type,
       name                            as name,
       trunc_sec(timestamp, 10)        as timestamp,
       f_sum(count(1))                 as count,
       sampling('Counter', header.msg) as sampling
from exception(name != 'io.netty.channel.unix.Errors$NativeIoException')
group by header.appId, name, type, header.ezone, header.hostName, header.cluster, trunc_sec(timestamp, 10);


@ Name ('service_exception')
@Metric(name = '{appId}.service_exception', tags = {'name', 'cluster', 'type','sourceType', 'method', 'ezone'}, fields = {'count'}, sampling = 'sampling')
@Metric(name = 'etrace.dashboard.service_exception', tags = {'appId', 'cluster', 'name', 'type', 'ezone'}, fields = {'count'})
select header.ezone                    as ezone,
       header.appId                    as appId,
       header.cluster                  as cluster,
       type                            as type,
       name                            as name,
       method                          as method,
       sourceType                      as sourceType,
       trunc_sec(timestamp, 10)        as timestamp,
       f_sum(count(1))                 as count,
       sampling('Counter', header.msg) as sampling
from exception(name != 'io.netty.channel.unix.Errors$NativeIoException' and method != 'unknown')
group by header.appId, name, type, method, sourceType, header.ezone, header.cluster, trunc_sec(timestamp, 10);
