package io.etrace.stream.aggregator.expression;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class EPClause {
    private List<ExpressionWrapper> wrappers;

    public EPClause(List<ExpressionWrapper> expressionWrapperBaseList) {
        this.wrappers = expressionWrapperBaseList;
    }

    public Set<String> items() {
        Set<String> items = newHashSet();
        for (ExpressionWrapper base : wrappers) {
            items.addAll(base.items());
        }
        return items;
    }

    public Set<String> asNames() {
        Set<String> asNames = newHashSet();
        for (ExpressionWrapper base : wrappers) {
            asNames.add(base.asName());
        }
        return asNames;
    }
}
