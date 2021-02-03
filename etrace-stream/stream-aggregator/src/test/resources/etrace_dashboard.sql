@Name('dashboard')
@Metric(name='{name}', fields={'count'})
@Metric(name='dashboard', fields={'count'})
select name, f_sum(sum(value)) as count
from mock_event
group by name;