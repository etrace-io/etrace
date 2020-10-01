package io.etrace.stream.aggregator;

import com.espertech.esper.client.soda.EPStatementObjectModel;
import io.etrace.stream.aggregator.expression.EPClause;
import io.etrace.stream.aggregator.expression.EPClauseFactory;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;

public class EPSelectClauseTest extends AbstractEPTest {
    @Test
    public void testAllProjectFunction() {
        testFunction("select sum(field('count')) as count from event group by measurementName", Collections.emptySet(),
            newHashSet("count"));
        testFunction("select min(field('count')) as count from event group by measurementName", Collections.emptySet(),
            newHashSet("count"));
        testFunction("select max(field('count')) as count from event group by measurementName", Collections.emptySet(),
            newHashSet("count"));
        testFunction("select avg(field('count')) as count from event group by measurementName", Collections.emptySet(),
            newHashSet("count"));
        testFunction("select median(field('count')) as count from event group by measurementName",
            Collections.emptySet(), newHashSet("count"));
    }

    @Test
    public void testAllUserDefineAggregateFunction() {
        testFunction("select hist(field('count')) as count from event group by measurementName", Collections.emptySet(),
            newHashSet("count"));
        testFunction("select fields_agg(fields) as fields from event group by measurementName", Collections.emptySet(),
            newHashSet("fields"));
        testFunction("select gauge(timestamp,field('count')) as gauge from event group by measurementName",
            Collections.emptySet(), newHashSet("gauge"));
        testFunction("select get_value(field('count')) as count from event group by measurementName",
            Collections.emptySet(), newHashSet("count"));
        testFunction("select group_count() as groupCount from event group by measurementName", Collections.emptySet(),
            newHashSet("groupCount"));
        testFunction("select sampling('Counter',header.msg) as sampling from event group by measurementName",
            Collections.emptySet(), newHashSet("sampling"));
        testFunction("select metricSampling(event) as sampling from event group by measurementName",
            Collections.emptySet(), newHashSet("sampling"));
    }

    @Test
    public void testAllSingleRowFunction() {
        testFunction("select f_sum(sum(field('count'))) as count from event group by measurementName",
            Collections.emptySet(), newHashSet("count"));
        testFunction("select f_gauge(gauge(timestamp, field('count'))) as count from event group by measurementName",
            Collections.emptySet(), newHashSet("count"));
        testFunction("select f_min(min(field('count'))) as count from event group by measurementName",
            Collections.emptySet(), newHashSet("count"));
        testFunction("select f_max(max(field('count'))) as count from event group by measurementName",
            Collections.emptySet(), newHashSet("count"));

        testFunction("select f_sum(field('count')) as count from event group by measurementName",
            newHashSet("f_sum(field('count'))"), newHashSet("count"));

        testFunction(
            "select name, tag('t1') as t1, tag('t2') as t2 from event group by metric_key(name, tag('t1'), tag('t2'))",
            newHashSet("name", "tag('t1')", "tag('t2')")
            , newHashSet("name", "t1", "t2"));
    }

    private void testFunction(String epl, Set<String> items, Set<String> asNames) {
        EPStatementObjectModel model = epEngine.compileEPL(epl);
        EPClause selectClause = EPClauseFactory.buildSelectClause(model);

        assertEquals(removeSpaces(items), selectClause.items());
        assertEquals(removeSpaces(asNames), selectClause.asNames());
    }

    private Set<String> removeSpaces(Set<String> strings) {
        return strings.stream().map(str -> str.replaceAll(" ", "")).collect(Collectors.toSet());
    }

}