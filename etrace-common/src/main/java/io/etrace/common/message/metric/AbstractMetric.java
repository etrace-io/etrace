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

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public abstract class AbstractMetric implements Serializable {

    protected String metricName;
    protected long timestamp;
    protected Map<String, String> tags;

    public void addTag(String tagKey, String tagValue) {
        tags.put(tagKey, tagValue);
    }

    public boolean hasTag(String tagKey) {
        return tags.containsKey(tagKey);
    }

    public String getTag(String tagKey) {
        return tags.get(tagKey);
    }

    public int calcKey() {
        int result = metricName != null ? metricName.hashCode() : 0;
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        return result;
    }

    public abstract AbstractMetric cloneMetric();
}
