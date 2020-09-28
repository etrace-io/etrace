package io.etrace.stream.aggregator.annotation;

import io.etrace.stream.aggregator.EPEngine;

/**
 * EP Engine aggregator time window config for shaka.biz.epl.app
 */
public @interface TimeWindow {
    int value() default EPEngine.DEFAULT_INTERVAL;
}
