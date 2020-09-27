/*
 * Copyright 2020 etrace.io
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

/**
 * Record the cost time of service
 * <p>
 * The timer doesn't support the quantile(upper_85,upper_90 etc.), but another metric type : Histogram do.
 * timer.setUpperEnable(false), means you get the metric type : Timer. Otherwise, it's Histogram.(default)
 * <p>
 * Timer has the following fields: avg, sum, count, min, max (avg = sum / count) Besides those fields, Histogram has
 * some more: upper_{\d+} : quantile(upper_85,upper_90 etc.);
 */
public interface Timer extends MetricInTraceApi<Timer> {

    /**
     * set this.value = (cost time), count = 1 As soon as value(long value) invoked, it means that this metric ends with
     * the value. The latter operations are all invalid.
     * <p>
     * For example: timer.value(100); // effective, the instance finishes. timer.end(); // invalid timer.value(100); //
     * invalid timer.addTag("key-2", "value-2"); // invalid timer.value(1);  //invalid
     *
     * @param value the time cost
     */
    void value(long value);

    /**
     * As soon as this method invoked, it means that this metric ends with the value. The latter operations are all
     * invalid.
     * <p>
     * For example: timer.end(); // effective, the instance finishes. timer.end(); // invalid timer.value(100); //
     * invalid timer.addTag("key-2", "value-2"); // invalid timer.value(1);  //invalid
     */
    void end();

    boolean isUpperEnable();

    /**
     * default: true, set the quantile enable
     * <p>
     * The default timer calculates the quantile(upper_85,upper_90 etc.), that means the metric type is: Histogram. When
     * do setUpperEnable(false), like : timer.setUpperEnable(false), then the metric type is: Timer .
     *
     * @param upperEnable the quantile enable
     * @return {@link Timer}
     */
    Timer setUpperEnable(boolean upperEnable);
}
