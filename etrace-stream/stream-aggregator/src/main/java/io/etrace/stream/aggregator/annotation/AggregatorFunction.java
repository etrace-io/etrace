package io.etrace.stream.aggregator.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AggregatorFunction {
    String name();
}
