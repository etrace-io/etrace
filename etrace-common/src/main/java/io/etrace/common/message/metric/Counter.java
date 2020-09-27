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
 * Counter has following fields: count to record the counter.
 * <p>
 * As soon as once() or value(long count) invoked, it means that this metric ends with the value.The latter operations
 * are all invalid.
 * <p>
 * For example: counter.once();  // effective, and the instance finishes. counter.addTag("key-2", "value-2"); // invalid
 * counter.once();  // invalid counter.value(10);  // invalid
 * <p>
 * Besides, value(long count) is the same as the once()
 */
public interface Counter extends MetricInTraceApi<Counter> {

    /**
     * set value = 1
     */
    void once();

    /**
     * set value = count
     *
     * @param count set count
     */
    void value(long count);
}
