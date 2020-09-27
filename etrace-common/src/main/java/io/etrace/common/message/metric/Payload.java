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
 * Record the size and count of package
 * <p>
 * Payload has the following field: avg, sum, count, min, max (avg = sum / count)
 */
public interface Payload extends MetricInTraceApi<Payload> {
    /**
     * As soon as value(double count) invoked, it means that this metric ends with the value. The latter operations are
     * all invalid. For example: payload.value(200); // effective, the instance finishes. payload.value(100); // invalid
     * payload.addTag("key-2", "value-2"); // invalid payload.value(1);  //invalid
     *
     * @param value package size
     */
    void value(long value);
}
