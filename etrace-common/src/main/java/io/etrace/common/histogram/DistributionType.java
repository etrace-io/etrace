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

package io.etrace.common.histogram;

public enum DistributionType {
    Percentile("percentile", (byte)0x1);

    private final String type;
    private final byte code;

    DistributionType(String type, byte code) {
        this.type = type;
        this.code = code;
    }

    public static DistributionType findByType(String type) {
        if (type == null) {
            throw new RuntimeException("Metric type is null");
        }
        switch (type.toLowerCase()) {
            case "percentile":
                return Percentile;
            default:
                throw new RuntimeException("Unknown distribution analyzer type for " + type);
        }
    }

    public static DistributionType findByCode(int code) {
        switch (code) {
            case ((byte)0x1):
                return Percentile;
            default:
                throw new RuntimeException("Unknown distribution analyzer code for " + code);
        }
    }

    public String type() {
        return type;
    }

    public byte code() {
        return code;
    }
}
