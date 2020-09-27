package io.etrace.common.constant;

public interface FieldName {

    /**
     * counter
     */
    String COUNTER_COUNT = "count";

    /**
     * gauge
     */
    String GAUGE_VALUE = "gauge";

    /**
     * timer
     */
    String TIMER_SUM = "timerSum";
    String TIMER_COUNT = "timerCount";
    String TIMER_MIN = "timerMin";
    String TIMER_MAX = "timerMax";
    String TIMER_UPPERENABLE = "upperEnable";

    /**
     * ratio
     */
    String RATIO_NUMERATOR = "numerator";
    String RATIO_DENOMINATOR = "denominator";

    /**
     * payload
     */
    String PAYLOAD_SUM = "payloadSum";
    String PAYLOAD_COUNT = "payloadCount";
    String PAYLOAD_MIN = "payloadMin";
    String PAYLOAD_MAX = "payloadMax";

    /**
     * histogram
     */
    String HISTOGRAM_PREFIX = "histogram";
    String HISTOGRAM_COUNT = "histogramCount";
    String HISTOGRAM_SUM = "histogramSum";
    String HISTOGRAM_MIN = "histogramMin";
    String HISTOGRAM_MAX = "histogramMax";
    String HISTOGRAM_FIELD_PREFIX = "histogramField";

    String UPPER_99 = "upper(99)";
    String UPPER_95 = "upper(95)";
    String UPPER_90 = "upper(90)";
    String UPPER_80 = "upper(80)";
    String UPPER = "upper";
    String UPPER_FIELD_START = "upper_";
    int FIELD_COUNT = 100;
    int DEFAULT_UPPER = 95;
}
