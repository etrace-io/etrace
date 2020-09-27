/*
 * Copyright 2019 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.common.message.metric;

public interface MetricFieldName {

    String COUNTER_COUNT = "count";
    String GAUGE_VALUE = "gauge";
    String TIMER_SUM = "timerSum";
    String TIMER_COUNT = "timerCount";
    String TIMER_MIN = "timerMin";
    String TIMER_MAX = "timerMax";
    String TIMER_UPPERENABLE = "upperEnable";
    String RATIO_NUMERATOR = "numerator";
    String RATIO_DENOMINATOR = "denominator";
    String PAYLOAD_SUM = "payloadSum";
    String PAYLOAD_COUNT = "payloadCount";
    String PAYLOAD_MIN = "payloadMin";
    String PAYLOAD_MAX = "payloadMax";

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
