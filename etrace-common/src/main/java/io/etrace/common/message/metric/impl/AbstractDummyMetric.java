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

package io.etrace.common.message.metric.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import io.etrace.common.message.metric.MetricInTraceApi;
import io.etrace.common.message.metric.field.MetricKey;

import java.io.IOException;

public abstract class AbstractDummyMetric<M> implements MetricInTraceApi<M> {
    private static final MetricKey key = new MetricKey();

    @Override
    public String getName() {
        return "";
    }

    public String getTopic() {
        return "";
    }

    @Override
    public MetricKey getKey() {
        return key;
    }

    @Override
    public MetricKey getTagKey() {
        return key;
    }

    public M setTopic(String topic) {
        return (M)this;
    }

    @Override
    public M addTag(String key, String value) {
        return (M)this;
    }

    @Override
    public void merge(MetricInTraceApi metricInTraceApi) {
    }

    @Override
    public void write(JsonGenerator generator) throws IOException {
    }
}
