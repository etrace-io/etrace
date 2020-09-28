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

package io.etrace.stream.aggregator;

import io.etrace.common.message.metric.Metric;
import io.etrace.common.message.metric.field.AggregateType;
import io.etrace.common.message.metric.field.Field;
import io.etrace.common.message.metric.field.MetricType;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class EPAggregatorEventListenerTest {

    // Metric{metricType=Timer, sampling='xxx', source='app_metric', fields={fgauge=Field{aggregateType=GAUGE,
    // value=100.0}, fsum=Field{aggregateType=SUM, value=100.0}, fmin=Field{aggregateType=MIN, value=100.0},
    // fmax=Field{aggregateType=MAX, value=100.0}}, metricName='name', timestamp=60000, tags={}}
    //==
    //Metric{metricType=Timer, sampling='yyy', source='app_metric', fields={fgauge=Field{aggregateType=GAUGE,
    // value=200.0}, fsum=Field{aggregateType=SUM, value=200.0}, fmin=Field{aggregateType=MIN, value=200.0},
    // fmax=Field{aggregateType=MAX, value=200.0}}, metricName='name', timestamp=60000, tags={}}

    // MetricKey{length=24, hash1=1826293986, hash2=-1995298636, hash3=1250712116}

    // MetricKey{length=24, hash1=1826293986, hash2=-1995298636, hash3=1250712116}

    @Test
    public void metricKey() {
        Metric m1 = new Metric();
        m1.setMetricType(MetricType.Timer);
        m1.setSampling("xxx");
        m1.setSource("app_metric");

        Map<String, Field> fields1 = new HashMap<>();
        fields1.put("fgauge", new Field(AggregateType.GAUGE, 100));
        fields1.put("fsum", new Field(AggregateType.SUM, 100));
        fields1.put("fmin", new Field(AggregateType.MIN, 100));
        fields1.put("fmax", new Field(AggregateType.MAX, 100));

        m1.setFields(fields1);
        m1.setMetricName("name");
        m1.setTimestamp(60000);
        m1.setTags(new HashMap());

        Metric m2 = new Metric();
        m2.setMetricType(MetricType.Timer);
        m2.setSampling("xxx");
        m2.setSource("app_metric");

        Map<String, Field> fields2 = new HashMap<>();
        fields2.put("fgauge", new Field(AggregateType.GAUGE, 200));
        fields2.put("fsum", new Field(AggregateType.SUM, 200));
        fields2.put("fmin", new Field(AggregateType.MIN, 200));
        fields2.put("fmax", new Field(AggregateType.MAX, 200));
        m2.setFields(fields2);

        m2.setMetricName("name");
        m2.setTimestamp(60000);
        m2.setTags(new HashMap());

        assertEquals(EPAggregatorEventListener.metricKey(m1),
            EPAggregatorEventListener.metricKey(m2));
    }

}