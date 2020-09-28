module stream.biz.app.url;

import io.etrace.stream.biz.app.event.*;

create schema url as URL;

@Name('url')
@Metric(name = '{appId}.url', tags = {'url', 'ezone', 'status'}, fields = {'timerCount', 'timerSum', 'timerMin', 'timerMax'}, sampling = 'sampling')
select header.ezone                            as ezone,
       header.appId                            as appId,
       url                                     as url,
       status                                  as status,
       trunc_sec(timestamp, 10)                as timestamp,
       f_sum(sum(duration))                    as timerSum,
       f_sum(count(1))                         as timerCount,
       f_max(max(duration))                    as timerMax,
       f_min(min(duration))                    as timerMin,
       sampling('Timer', duration, header.msg) as sampling
from url
group by header.appId, url, status, header.ezone, trunc_sec(timestamp, 10);
