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

package io.etrace.common.message.metric.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.etrace.common.message.metric.Metric;
import io.etrace.common.message.metric.MetricFieldName;
import io.etrace.common.message.metric.MetricHeader;
import io.etrace.common.message.metric.MetricMessage;
import io.etrace.common.message.metric.field.AggregateType;
import io.etrace.common.message.metric.field.Field;
import io.etrace.common.message.metric.field.MetricType;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LegacyVersionMetricCodecTest {

    public static Map<String, String> newTreeMap(String... list) {
        Map<String, String> map = new TreeMap<>();
        for (int i = 0; i < list.length - 1; i += 2) {
            map.put(list[i], list[i + 1]);
        }
        return map;
    }

    @Test
    public void v1ToV1ByCodecV1() throws IOException {
        String metricData = "[[\"#v1#t1\",\"topic\",\"appId\",\"hostIp\",\"hostName\",{\"cluster\":\"cluster\","
            + "\"ezone\":\"ezone\",\"idc\":\"idc\",\"mesosTaskId\":\"mesosTaskId\","
            + "\"eleapposLabel\":\"eleapposLabel\",\"eleapposSlaveFqdn\":\"eleapposSlaveFqdn\"},[[\"counter\","
            + "\"counter.name\",12345,{\"a\":\"b\",\"c\":\"d\"},4],[\"gauge\",\"gauge.name\",12345,{\"a\":\"b\","
            + "\"c\":\"d\"},4],[\"timer\",\"timer.name\",12345,{\"a\":\"b\",\"c\":\"d\"},4,5,6,7,8],[\"payload\","
            + "\"payload.name\",12345,{\"a\":\"b\",\"c\":\"d\"},4,5,6,7],[\"ratio\",\"ratio.name\",12345,"
            + "{\"a\":\"b\",\"c\":\"d\"},4,5],[\"metric\",\"metric.name\",12345,{\"a\":\"b\",\"c\":\"d\"},"
            + "[[\"count\",\"SUM\",10],[\"gauge\",\"GAUGE\",10.1]]],[\"histogram\",\"histogram1.name\",12345,"
            + "{\"a\":\"b\",\"c\":\"d\"},4,5,6,7,8,9,[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,"
            + "24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,"
            + "58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,"
            + "92,93,94,95,96,97,98,99]],[\"histogram\",\"histogram2.name\",12345,{\"a\":\"b\",\"c\":\"d\"},4,5,6,7,"
            + "8,9,{\"99\":99,\"1\":1,\"50\":50}]]],[\"topic\",\"appId\",\"hostIp\",\"hostName\","
            + "{\"cluster\":\"cluster\",\"ezone\":\"ezone\",\"idc\":\"idc\",\"mesosTaskId\":\"mesosTaskId\","
            + "\"eleapposLabel\":\"eleapposLabel\",\"eleapposSlaveFqdn\":\"eleapposSlaveFqdn\"},[[\"counter\","
            + "\"counter.name\",12345,{\"a\":\"b\",\"c\":\"d\"},4],[\"gauge\",\"gauge.name\",12345,{\"a\":\"b\","
            + "\"c\":\"d\"},4],[\"timer\",\"timer.name\",12345,{\"a\":\"b\",\"c\":\"d\"},4,5,6,7,8],[\"payload\","
            + "\"payload.name\",12345,{\"a\":\"b\",\"c\":\"d\"},4,5,6,7],[\"ratio\",\"ratio.name\",12345,"
            + "{\"a\":\"b\",\"c\":\"d\"},4,5],[\"metric\",\"metric.name\",12345,{\"a\":\"b\",\"c\":\"d\"},"
            + "[[\"count\",\"SUM\",10],[\"gauge\",\"GAUGE\",10.1]]],[\"histogram\",\"histogram1.name\",12345,"
            + "{\"a\":\"b\",\"c\":\"d\"},4,5,6,7,8,9,[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,"
            + "24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,"
            + "58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,"
            + "92,93,94,95,96,97,98,99]],[\"histogram\",\"histogram2.name\",12345,{\"a\":\"b\",\"c\":\"d\"},4,5,6,7,"
            + "8,9,{\"99\":99,\"1\":1,\"50\":50}]]]]";

        //System.out.println(metricData);
        ObjectMapper mapper = new ObjectMapper();

        //FramedMetricMessageCodecV1 codecV1 = new FramedMetricMessageCodecV1();
        //MetricMessage message = codecV1.decodeToLegacyVersion( mapper.writeValueAsString(metricData).getBytes());

        //todo: add new format test
        // 一时半会还弄不了，因为还没法将 encode metrics的流程 分离出来
    }

    @Test
    public void v1ToLegacyByCodecV1() {
        //todo: add new format test
        // 一时半会还弄不了，因为还没法将 encode metrics的流程 分离出来
    }

    @Test
    public void legacyToLegacyByCodecV1_111() throws IOException {
        String msg = "[[\"#v1#t2\",null,\"me.ele.arch.etrace.collector\",\"127.0.0.1\",\"Pro"
            + ".local\",null,[[\"timer\",\"task.process.duration\",1590027515721,{\"name\":\"trace_processor\","
            + "\"pipeline\":\"collector\",\"task\":\"collector-trace_processor-1\"},13206,2258,13206,98,0],"
            + "[\"timer\",\"task.process.duration\",1590027515721,{\"name\":\"trace_processor\","
            + "\"pipeline\":\"collector\",\"task\":\"collector-trace_processor-0\"},13185,2261,13185,98,0],"
            + "[\"timer\",\"task.process.duration\",1590027515720,{\"name\":\"dal_processor\","
            + "\"pipeline\":\"collector\",\"task\":\"collector-dal_processor-0\"},13180,2259,13180,100,0],[\"timer\","
            + "\"task.process.duration\",1590027515721,{\"name\":\"dal_processor\",\"pipeline\":\"collector\","
            + "\"task\":\"collector-dal_processor-1\"},13212,2262,13212,98,0],[\"timer\",\"task.process.duration\","
            + "1590027515721,{\"name\":\"metric_processor\",\"pipeline\":\"collector\","
            + "\"task\":\"collector-metric_processor-0\"},13208,2254,13208,99,0],[\"timer\",\"task.process"
            + ".duration\",1590027515721,{\"name\":\"metric_processor\",\"pipeline\":\"collector\","
            + "\"task\":\"collector-metric_processor-1\"},13198,2257,13198,99,0]]]]\n"
            + "\n"
            + "\n";
        MetricCodecV1 decoder = new MetricCodecV1();
        List<MetricMessage> metricMessageList = decoder.decodeToMetricMessageLegacyVersion(msg.getBytes());
    }

    @Test
    public void legacyToLegacyByCodecV1() throws IOException {
        MetricCodecV1 decoder = new MetricCodecV1();
        String msg = RawMetricMessageGenerator.combinedMetricMessageGenerator(2, false);

        //System.out.println(msg);

        List<MetricMessage> metricMessageList = decoder.decodeToMetricMessageLegacyVersion(msg.getBytes());
        judgeOne(metricMessageList.get(0));
        judgeOne(metricMessageList.get(1));
    }

    public void judgeOne(MetricMessage metricMessage) {

        Map<String, String> targetMap = newTreeMap("a", "b", "c", "d");

        MetricHeader header = metricMessage.getMetricHeader();
        List<Metric> metrics = metricMessage.getMetrics();

        Assert.assertEquals("topic", header.getTopic());
        Assert.assertEquals("appId", header.getAppId());
        Assert.assertEquals("hostIp", header.getHostIp());
        Assert.assertEquals("hostName", header.getHostName());
        Assert.assertEquals("cluster", header.getCluster());
        Assert.assertEquals("ezone", header.getEzone());
        Assert.assertEquals("idc", header.getIdc());
        Assert.assertEquals("mesosTaskId", header.getMesosTaskId());
        Assert.assertEquals("eleapposLabel", header.getEleapposLabel());
        Assert.assertEquals("eleapposSlaveFqdn", header.getEleapposSlaveFqdn());

        // counter
        Metric counterMetric = metrics.get(0);
        Assert.assertEquals(MetricType.Counter, counterMetric.getMetricType());
        Assert.assertEquals("counter.name", counterMetric.getMetricName());
        Assert.assertEquals(12345, counterMetric.getTimestamp());
        Assert.assertEquals(targetMap, counterMetric.getTags());
        Assert.assertEquals(new Field(AggregateType.SUM, 4l),
            counterMetric.getFields().get(MetricFieldName.COUNTER_COUNT));
        Assert.assertEquals(1, counterMetric.getFields().size());

        // gauge
        Metric gaugeMetric = metrics.get(1);
        Assert.assertEquals(MetricType.Gauge, gaugeMetric.getMetricType());
        Assert.assertEquals("gauge.name", gaugeMetric.getMetricName());
        Assert.assertEquals(12345, gaugeMetric.getTimestamp());
        Assert.assertEquals(targetMap, gaugeMetric.getTags());
        Assert.assertEquals(new Field(AggregateType.GAUGE, 4.),
            gaugeMetric.getFields().get(MetricFieldName.GAUGE_VALUE));
        Assert.assertEquals(1, gaugeMetric.getFields().size());

        // timer
        Metric timerMetric = metrics.get(2);
        Assert.assertEquals(MetricType.Timer, timerMetric.getMetricType());
        Assert.assertEquals("timer.name", timerMetric.getMetricName());
        Assert.assertEquals(12345, timerMetric.getTimestamp());
        Assert.assertEquals(targetMap, timerMetric.getTags());
        Assert.assertEquals(new Field(AggregateType.SUM, 4l), timerMetric.getFields().get(MetricFieldName.TIMER_SUM));
        Assert.assertEquals(new Field(AggregateType.SUM, 5l), timerMetric.getFields().get(MetricFieldName.TIMER_COUNT));
        Assert.assertEquals(new Field(AggregateType.MIN, 6l), timerMetric.getFields().get(MetricFieldName.TIMER_MIN));
        Assert.assertEquals(new Field(AggregateType.MAX, 7l), timerMetric.getFields().get(MetricFieldName.TIMER_MAX));
        //        Assert.assertEquals(new Field(AggregateType.MAX, 8l), timerMetric.getFields().get(FieldName
        //        .TIMER_UPPERENABLE));
        Assert.assertEquals(4, timerMetric.getFields().size());

        // payload
        Metric payloadMetric = metrics.get(3);
        Assert.assertEquals(MetricType.Payload, payloadMetric.getMetricType());
        Assert.assertEquals("payload.name", payloadMetric.getMetricName());
        Assert.assertEquals(12345, payloadMetric.getTimestamp());
        Assert.assertEquals(targetMap, payloadMetric.getTags());
        Assert.assertEquals(new Field(AggregateType.SUM, 4l),
            payloadMetric.getFields().get(MetricFieldName.PAYLOAD_SUM));
        Assert.assertEquals(new Field(AggregateType.SUM, 5l),
            payloadMetric.getFields().get(MetricFieldName.PAYLOAD_COUNT));
        Assert.assertEquals(new Field(AggregateType.MIN, 6l),
            payloadMetric.getFields().get(MetricFieldName.PAYLOAD_MIN));
        Assert.assertEquals(new Field(AggregateType.MAX, 7l),
            payloadMetric.getFields().get(MetricFieldName.PAYLOAD_MAX));
        Assert.assertEquals(4, payloadMetric.getFields().size());

        // ratio
        Metric ratioMetric = metrics.get(4);
        Assert.assertEquals(MetricType.Ratio, ratioMetric.getMetricType());
        Assert.assertEquals("ratio.name", ratioMetric.getMetricName());
        Assert.assertEquals(12345, ratioMetric.getTimestamp());
        Assert.assertEquals(targetMap, ratioMetric.getTags());
        Assert.assertEquals(new Field(AggregateType.SUM, 4l),
            ratioMetric.getFields().get(MetricFieldName.RATIO_NUMERATOR));
        Assert.assertEquals(new Field(AggregateType.SUM, 5l),
            ratioMetric.getFields().get(MetricFieldName.RATIO_DENOMINATOR));
        Assert.assertEquals(2, ratioMetric.getFields().size());

        // metric
        Metric metricMetric = metrics.get(5);
        Assert.assertEquals(MetricType.Metric, metricMetric.getMetricType());
        Assert.assertEquals("metric.name", metricMetric.getMetricName());
        Assert.assertEquals(12345, metricMetric.getTimestamp());
        Assert.assertEquals(targetMap, metricMetric.getTags());
        Assert.assertEquals(new Field(AggregateType.SUM, 10l),
            metricMetric.getFields().get(MetricFieldName.COUNTER_COUNT));
        Assert.assertEquals(new Field(AggregateType.GAUGE, 10.10d),
            metricMetric.getFields().get(MetricFieldName.GAUGE_VALUE));
        Assert.assertEquals(2, metricMetric.getFields().size());

        Metric histogram1Metric = metrics.get(6);
        Assert.assertEquals(MetricType.Histogram, histogram1Metric.getMetricType());
        Assert.assertEquals("histogram1.name", histogram1Metric.getMetricName());
        Assert.assertEquals(12345, histogram1Metric.getTimestamp());
        Assert.assertEquals(targetMap, histogram1Metric.getTags());
        Assert.assertEquals(new Field(AggregateType.MIN, 5l),
            histogram1Metric.getFields().get(MetricFieldName.HISTOGRAM_MIN));
        Assert.assertEquals(new Field(AggregateType.MAX, 6l),
            histogram1Metric.getFields().get(MetricFieldName.HISTOGRAM_MAX));
        Assert.assertEquals(new Field(AggregateType.SUM, 7l),
            histogram1Metric.getFields().get(MetricFieldName.HISTOGRAM_SUM));
        Assert.assertEquals(new Field(AggregateType.SUM, 8l),
            histogram1Metric.getFields().get(MetricFieldName.HISTOGRAM_COUNT));
        for (int i = 1; i < 100; i++) {
            Assert.assertEquals(new Field(AggregateType.SUM, i),
                histogram1Metric.getFields().get(MetricFieldName.HISTOGRAM_FIELD_PREFIX + i));
        }
        Assert.assertEquals(99 + 4, histogram1Metric.getFields().size());

        Metric histogram2Metric = metrics.get(7);
        Assert.assertEquals(MetricType.Histogram, histogram2Metric.getMetricType());
        Assert.assertEquals("histogram2.name", histogram2Metric.getMetricName());
        Assert.assertEquals(12345, histogram2Metric.getTimestamp());
        Assert.assertEquals(targetMap, histogram2Metric.getTags());
        Assert.assertEquals(new Field(AggregateType.MIN, 5l),
            histogram2Metric.getFields().get(MetricFieldName.HISTOGRAM_MIN));
        Assert.assertEquals(new Field(AggregateType.MAX, 6l),
            histogram2Metric.getFields().get(MetricFieldName.HISTOGRAM_MAX));
        Assert.assertEquals(new Field(AggregateType.SUM, 7l),
            histogram2Metric.getFields().get(MetricFieldName.HISTOGRAM_SUM));
        Assert.assertEquals(new Field(AggregateType.SUM, 8l),
            histogram2Metric.getFields().get(MetricFieldName.HISTOGRAM_COUNT));
        Assert.assertEquals(new Field(AggregateType.SUM, 1l),
            histogram2Metric.getFields().get(MetricFieldName.HISTOGRAM_FIELD_PREFIX + 1));
        Assert.assertEquals(new Field(AggregateType.SUM, 50l),
            histogram2Metric.getFields().get(MetricFieldName.HISTOGRAM_FIELD_PREFIX + 50));
        Assert.assertEquals(new Field(AggregateType.SUM, 99l),
            histogram2Metric.getFields().get(MetricFieldName.HISTOGRAM_FIELD_PREFIX + 99));

        Assert.assertEquals(7, histogram2Metric.getFields().size());

    }

}
