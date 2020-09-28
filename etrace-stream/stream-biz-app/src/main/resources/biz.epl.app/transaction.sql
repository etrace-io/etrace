module stream.biz.app.tranaction;

import io.etrace.stream.biz.app.event.*;

create schema transaction as Transaction;

@Name('transaction')
@Metric(name = '{appId}.transaction', tags = {'type', 'name', 'status', 'ezone', 'hostName'}, fields = {'timerCount', 'timerSum', 'timerMin', 'timerMax'}, sampling = 'sampling')
select header.ezone                            as ezone,
       header.appId                            as appId,
       header.hostName                         as hostName,
       type                                    as type,
       name                                    as name,
       status                                  as status,
       trunc_sec(timestamp, 10)                as timestamp,
       f_sum(sum(duration))                    as timerSum,
       f_sum(count(1))                         as timerCount,
       f_max(max(duration))                    as timerMax,
       f_min(min(duration))                    as timerMin,
       sampling('Timer', duration, header.msg) as sampling
from transaction
group by header.appId, type, name, header.hostName, header.ezone, status, trunc_sec(timestamp, 10);
