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

package io.etrace.common.message.metric.field;

/**
 * 指标类型
 */
public enum MetricType {

    /**
     * 计数器
     */
    Counter {
        @Override
        public String toIdentifier() {
            return COUNTER;
        }

        @Override
        public byte code() {
            return (byte)0x01;
        }
    },
    /**
     * 计
     */
    Gauge {
        @Override
        public String toIdentifier() {
            return GAUGE;
        }

        @Override
        public byte code() {
            return (byte)0x02;
        }
    },
    /**
     * 计时器
     */
    Timer {
        @Override
        public String toIdentifier() {
            return TIMER;
        }

        @Override
        public byte code() {
            return (byte)0x03;
        }
    },
    /**
     * 比
     */
    Ratio {
        @Override
        public String toIdentifier() {
            return RATIO;
        }

        @Override
        public byte code() {
            return (byte)0x04;
        }
    },
    /**
     * 有效载荷
     */
    Payload {
        @Override
        public String toIdentifier() {
            return PAYLOAD;
        }

        @Override
        public byte code() {
            return (byte)0x06;
        }
    },

    /**
     * 柱状图
     */
    Histogram {
        @Override
        public String toIdentifier() {
            return HISTOGRAM;
        }

        @Override
        public byte code() {
            return (byte)0x07;
        }
    },
    /**
     * 度规
     */
    Metric {
        @Override
        public String toIdentifier() {
            return METRIC;
        }

        @Override
        public byte code() {
            return (byte)0x08;
        }
    };

    public static final String COUNTER = "counter";
    public static final String GAUGE = "gauge";
    public static final String TIMER = "timer";
    public static final String PAYLOAD = "payload";
    public static final String RATIO = "ratio";
    public static final String HISTOGRAM = "histogram";
    public static final String METRIC = "metric";

    public static MetricType fromIdentifier(String identifier) {
        switch (identifier) {
            case COUNTER:
                return Counter;
            case GAUGE:
                return Gauge;
            case TIMER:
                return Timer;
            case PAYLOAD:
                return Payload;
            case RATIO:
                return Ratio;
            case HISTOGRAM:
                return Histogram;
            case METRIC:
                return Metric;
        }
        return null;
    }

    public abstract String toIdentifier();

    public abstract byte code();
}
