module groupCount;

@Name("group_count")
@Metric(name="name", fields={'timerCount'})
select name,
       f_sum(count(1)) as timerCount
from mock_event
group by name;




