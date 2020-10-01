package io.etrace.stream.aggregator;

import com.espertech.esper.client.soda.EPStatementObjectModel;
import com.espertech.esper.client.soda.SelectClauseElement;
import com.espertech.esper.client.soda.SelectClauseExpression;
import io.etrace.stream.aggregator.expression.ExpressionWrapperBase;
import io.etrace.stream.aggregator.expression.ExpressionWrapperFactory;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ExpressionWrapperFactoryTest extends AbstractEPTest {

    @Test
    public void testAggregateExpressionWrapper() {
        testExpressionWrapper("select sum(fields('count')) as count from event group by name", "count",
            Collections.emptySet());
        testExpressionWrapper("select min(fields('count')) as count from event group by name", "count",
            Collections.emptySet());
        testExpressionWrapper("select max(fields('count')) as count from event group by name", "count",
            Collections.emptySet());
        testExpressionWrapper("select count(fields('count')) as count from event group by name", "count",
            Collections.emptySet());
        testExpressionWrapper("select hist(fields('count')) as hist from event group by name", "hist",
            Collections.emptySet());
    }

    @Test
    public void testConstantExpressionWrapper() {
        testExpressionWrapper("select 'name' as name from event group by name", "name", Collections.emptySet());
        testExpressionWrapper("select 1 as count from event group by name", "count", Collections.emptySet());
    }

    @Test
    public void testPropertyValueExpressionWrapper() {
        testExpressionWrapper("select name as name from event group by name", "name", newHashSet("name"));
        testExpressionWrapper("select tags('name') as name from event group by tags('name')", "name",
            newHashSet("tags('name')"));
        testExpressionWrapper("select tag('name') as name from event group by tag('name')", "name",
            newHashSet("tag('name')"));
    }

    @Test
    public void testSingleRowFunctionExpressionWrapper() {
        testExpressionWrapper("select f_sum(sum(fields('count'))) as count from event group by name", "count",
            Collections.emptySet());
        testExpressionWrapper("select f_sum(fields('count')) as count from event group by name", "count",
            newHashSet("f_sum(fields('count'))"));
        testExpressionWrapper("select trunc_sec(timestamp, 10) as ts from event group by name", "ts",
            newHashSet("trunc_sec(timestamp, 10)"));
    }

    private void testExpressionWrapper(String epl, String asName, Set<String> items) {

        EPStatementObjectModel model = epEngine.compileEPL(epl);

        assertNotNull(model.getSelectClause());

        assertFalse(model.getSelectClause().getSelectList().isEmpty());

        SelectClauseElement selectClauseElement = model.getSelectClause().getSelectList().get(0);

        assertTrue(selectClauseElement instanceof SelectClauseExpression);

        SelectClauseExpression selectClauseExpression = (SelectClauseExpression)selectClauseElement;

        ExpressionWrapperBase expressionWrapperBase = ExpressionWrapperFactory.build(
            selectClauseExpression.getExpression(), selectClauseExpression.getAsName());

        assertThat(expressionWrapperBase.asName(), is(asName));

        items = items.stream().map(str ->
            str.replaceAll(" ", "")
        ).collect(Collectors.toSet());

        assertThat(expressionWrapperBase.items(), is(items));
    }

}