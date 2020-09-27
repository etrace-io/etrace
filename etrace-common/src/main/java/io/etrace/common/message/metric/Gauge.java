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
 * Gauge has the field: value to record the last value in the time point
 */
public interface Gauge extends MetricInTraceApi<Gauge> {
    /**
     * As soon as value(double count) invoked, it means that this metric ends with the value. The latter operations are
     * all invalid. For example: gauge.value(200); // effective, the instance finishes. gauge.value(100); // invalid
     * gauge.addTag("key-2", "value-2"); // invalid gauge.value(1);  //invalid
     *
     * @param value the last value
     */
    void value(double value);
}
