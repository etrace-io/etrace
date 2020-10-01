package io.etrace.stream.aggregator.expression;

import com.espertech.esper.client.soda.Expression;
import com.espertech.esper.client.soda.ExpressionPrecedenceEnum;

import java.io.StringWriter;

public class EPClausUtils {
    private EPClausUtils() {
    }

    public static String getItem(Expression expression) {
        StringWriter writer = new StringWriter();
        expression.toEPL(writer, ExpressionPrecedenceEnum.MINIMUM);
        return writer.toString();
    }
}
