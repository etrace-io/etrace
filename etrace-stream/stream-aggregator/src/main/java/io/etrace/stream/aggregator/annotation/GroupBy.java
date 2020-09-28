package io.etrace.stream.aggregator.annotation;

/**
 * 1. 有groupby注解 router key = metric hash
 * <p>
 * 2. 无gropby router key == null
 */
public @interface GroupBy {
    String value() default "";
}
