package io.etrace.stream.aggregator.expression;

import java.util.Set;

public interface ExpressionWrapper {
    String asName();

    Set<String> items();
}

