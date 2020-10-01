package io.etrace.stream.aggregator;

import com.espertech.esper.client.soda.EPStatementObjectModel;
import io.etrace.stream.aggregator.expression.EPClause;
import io.etrace.stream.aggregator.expression.EPClauseFactory;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;

public class EPGroupByClauseTest extends AbstractEPTest {

    @Test
    public void testGroupBy() {
        testFunction("select count(fields('count')) as count from event group by name, tags('t1'), tag('t2')",
            newHashSet("name", "tags('t1')", "tag('t2')"));
        testFunction(
            "select count(fields('count')) as count from event group by name, metric_key(tags('t1'), tag('t2'))",
            newHashSet("name", "tags('t1')", "tag('t2')"));
        testFunction("select count(fields('count')) as count from event group by name, trunc_sec(timestamp, 10)",
            newHashSet("name", "trunc_sec(timestamp, 10)"));
    }

    private void testFunction(String epl, Set<String> items) {
        EPStatementObjectModel model = epEngine.compileEPL(epl);
        EPClause selectClause = EPClauseFactory.buildGroupByClaus(model);

        org.junit.Assert.assertEquals(removeSpaces(items), selectClause.items());
    }

    private Set<String> removeSpaces(Set<String> strings) {
        return strings.stream().map(str -> str.replaceAll(" ", "")).collect(Collectors.toSet());
    }
}
