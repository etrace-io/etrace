module stream.biz.app.jvm;

import io.etrace.stream.biz.app.event.*;

create schema jvm as JVM;
create schema jvm_memory_pool as JVMMemoryPool;

@Name('jvm_class_load')
@Metric(name = '{appId}.jvm_class_load', tags = {'hostName', 'name', 'ezone'}, fields = {'gauge'})
@TimeWindow(60)
select header.ezone                     as ezone,
       header.appId                     as appId,
       header.hostName                  as hostName,
       name                             as name,
       trunc_sec(timestamp, 60)         as timestamp,
       f_gauge(gauge(timestamp, value)) as gauge
from jvm(type = 'jvm.loaded.classes')
group by header.appId, name, header.hostName, header.ezone, trunc_sec(timestamp, 60);

@Name('jvm_cpu')
@Metric(name = '{appId}.jvm_cpu', tags = {'hostName', 'name', 'ezone'}, fields = {'gauge'})
@TimeWindow(60)
select header.ezone                     as ezone,
       header.appId                     as appId,
       header.hostName                  as hostName,
       name                             as name,
       trunc_sec(timestamp, 60)         as timestamp,
       f_gauge(gauge(timestamp, value)) as gauge
from jvm(type = 'jvm.cpu')
group by header.appId, name, header.hostName, header.ezone, trunc_sec(timestamp, 60);


@Name('jvm_gc_count')
@Metric(name = '{appId}.jvm_gc_count', tags = {'hostName', 'name', 'ezone'}, fields = {'count'})
@TimeWindow(60)
select header.ezone             as ezone,
       header.appId             as appId,
       header.hostName          as hostName,
       name                     as name,
       trunc_sec(timestamp, 60) as timestamp,
       f_sum(sum(value))        as count
from jvm(type = 'jvm.garbage.count')
group by header.appId, name, header.hostName, header.ezone, trunc_sec(timestamp, 60);

@Name('jvm_gc_time')
@Metric(name = '{appId}.jvm_gc_timer', tags = {'hostName', 'name', 'ezone'}, fields = {'count'})
@TimeWindow(60)
select header.ezone             as ezone,
       header.appId             as appId,
       header.hostName          as hostName,
       name                     as name,
       trunc_sec(timestamp, 60) as timestamp,
       f_sum(sum(value))        as count
from jvm(type = 'jvm.garbage.time')
group by header.appId, name, header.hostName, header.ezone, trunc_sec(timestamp, 60);

@Name('jvm_memory')
@Metric(name = '{appId}.jvm_memory', tags = {'hostName', 'name', 'ezone'}, fields = {'gauge'})
@TimeWindow(60)
select header.ezone                     as ezone,
       header.appId                     as appId,
       header.hostName                  as hostName,
       name                             as name,
       trunc_sec(timestamp, 60)         as timestamp,
       f_gauge(gauge(timestamp, value)) as gauge
from jvm(type = 'jvm.memory')
group by header.appId, header.hostName, name, header.ezone, trunc_sec(timestamp, 60);

@Name('jvm_memory_pool')
@Metric(name = '{appId}.jvm_memory_pool', tags = {'hostName', 'name', 'subType', 'ezone'}, fields = {'gauge'})
@TimeWindow(60)
select header.ezone                     as ezone,
       header.appId                     as appId,
       header.hostName                  as hostName,
       name                             as name,
       subType                          as subType,
       trunc_sec(timestamp, 60)         as timestamp,
       f_gauge(gauge(timestamp, value)) as gauge
from jvm_memory_pool(type = 'jvm.memory.pool')
group by header.appId, header.hostName, name, subType, header.ezone, trunc_sec(timestamp, 60);

@Name('jvm_thread')
@Metric(name = '{appId}.jvm_thread', tags = {'hostName', 'name', 'ezone'}, fields = {'gauge'})
@TimeWindow(60)
select header.ezone                     as ezone,
       header.appId                     as appId,
       header.hostName                  as hostName,
       name                             as name,
       trunc_sec(timestamp, 60)         as timestamp,
       f_gauge(gauge(timestamp, value)) as gauge
from jvm(type = 'jvm.thread')
group by header.appId, name, header.hostName, header.ezone, trunc_sec(timestamp, 60);
