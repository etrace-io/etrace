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

package io.etrace.common.message.metric.field;

public class MetricKey {
    private static final int ISOLATE_HASH2 = "##".hashCode();
    private static final int ISOLATE_HASH3 = "@@".hashCode();
    private static final int TRUE_HASH = "true".hashCode();
    private static final int FALSE_HASH = "false".hashCode();
    private static final int TRUE_LENGTH = "true".length();
    private static final int FALSE_LENGTH = "false".length();

    int length;
    int hash1;
    int hash2;
    int hash3;

    public MetricKey() {
    }

    public void add(boolean b) {
        if (b) {
            add(TRUE_LENGTH, TRUE_HASH);
        } else {
            add(FALSE_LENGTH, FALSE_HASH);
        }
    }

    public void add(String value) {
        if (value == null) {
            return;
        }
        int valueHash = value != null ? value.hashCode() : 0;
        add(value.length(), valueHash);
    }

    private void add(int length, int hash) {
        this.length += length;
        hash1 = 31 * hash1 + hash;
        hash2 = 31 * hash2 + ISOLATE_HASH2;
        hash2 = 31 * hash2 + hash;
        hash3 = 31 * hash3 + ISOLATE_HASH3;
        hash3 = 31 * hash3 + hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        MetricKey metricKey = (MetricKey)o;
        if (length != metricKey.length) { return false; }
        if (hash1 != metricKey.hash1) { return false; }
        if (hash2 != metricKey.hash2) { return false; }
        return hash3 == metricKey.hash3;
    }

    @Override
    public int hashCode() {
        return hash2;
    }
}
