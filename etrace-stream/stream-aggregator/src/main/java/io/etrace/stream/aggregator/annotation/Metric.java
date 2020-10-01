package io.etrace.stream.aggregator.annotation;

public @interface Metric {
    String name() default "";

    String[] tags() default {};

    String[] fields() default {};

    // fieldName -> field pair to rename select field
    String[] fieldMap() default {};

    String sampling() default "";

    String source() default "";
}
