module stream.biz.app.event;

import io.etrace.stream.biz.app.event.*;

create schema event as Event;

@Name('event')
@Metric(name = '{appId}.event', tags = {'type', 'name', 'ezone', 'status', 'hostName'}, fields = {'count'}, sampling = 'sampling')
select header.ezone                    as ezone,
       header.appId                    as appId,
       header.hostName                 as hostName,
       type                            as type,
       name                            as name,
       status                          as status,
       trunc_sec(timestamp, 10)        as timestamp,
       f_sum(count(1))                 as count,
       sampling('Counter', header.msg) as sampling
from event
group by header.appId, type, name, header.hostName, header.ezone, status, trunc_sec(timestamp, 10);
