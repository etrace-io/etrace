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
import io.etrace.common.util.JSONUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RawMetricMessageGenerator {

    private final static ObjectMapper mapper = new ObjectMapper();
    private final static String[] types = new String[] {"counter", "gauge", "timer", "payload", "ratio", "metric",
        "histogram1", "histogram2"};

    public static Object[] metricGenerator(int index, boolean needRandom) throws IOException {
        String type = types[index % types.length];
        Object[] metric = null;
        if (needRandom) {
            Random rand = new Random();
            switch (type) {
                case "counter":
                    metric = new Object[] {"counter", "counter.name", 12345, newMap("a", "b", "c", "d"), rand.nextInt(
                        1000)};
                    break;
                case "gauge":
                    metric = new Object[] {"gauge", "gauge.name", 12345, newMap("a", "b", "c", "d"), rand.nextDouble()};
                    break;
                case "timer":
                    metric = new Object[] {"timer", "timer.name", 12345, newMap("a", "b", "c", "d"), rand.nextInt(1000),
                        rand.nextInt(1000), rand.nextInt(1000), rand.nextInt(1000), 1};
                    break;
                case "payload":
                    metric = new Object[] {"payload", "payload.name", 12345, newMap("a", "b", "c", "d"), rand.nextInt(
                        1000), rand.nextInt(1000), rand.nextInt(1000), rand.nextInt(1000)};
                    break;
                case "ratio":
                    metric = new Object[] {"ratio", "ratio.name", 12345, newMap("a", "b", "c", "d"), rand.nextInt(1000),
                        rand.nextInt(1000)};
                    break;
                case "metric":
                    Object[] fields = new Object[2];
                    fields[0] = new Object[] {"count", "SUM", rand.nextInt(1000)};
                    fields[1] = new Object[] {"gauge", "GAUGE", rand.nextDouble()};
                    metric = new Object[] {"metric", "metric.name", 12345, newMap("a", "b", "c", "d"), fields};
                    break;
                case "histogram1":
                    Object[] hFields = new Object[100];
                    for (int i = 0; i < 100; i++) {
                        hFields[i] = rand.nextInt(1000);
                    }
                    metric = new Object[] {"histogram", "histogram1.name", 12345, newMap("a", "b", "c", "d"), 4, 5, 6,
                        7, 8, 9, hFields};
                    break;
                case "histogram2":
                    Map<String, Integer> hFieldMap = new HashMap<>();
                    for (int i = 0; i < rand.nextInt(90) + 2; i++) {
                        hFieldMap.put(String.valueOf(rand.nextInt(90) + 2), rand.nextInt(90) + 2);
                    }
                    metric = new Object[] {"histogram", "histogram2.name", 12345, newMap("a", "b", "c", "d"), 4, 5, 6,
                        7, 8, 9, hFieldMap};
                    break;
            }
        } else {
            switch (type) {
                case "counter":
                    metric = new Object[] {"counter", "counter.name", 12345, newMap("a", "b", "c", "d"), 4};
                    break;
                case "gauge":
                    metric = new Object[] {"gauge", "gauge.name", 12345, newMap("a", "b", "c", "d"), 4};
                    break;
                case "timer":
                    metric = new Object[] {"timer", "timer.name", 12345, newMap("a", "b", "c", "d"), 4, 5, 6, 7, 8};
                    break;
                case "payload":
                    metric = new Object[] {"payload", "payload.name", 12345, newMap("a", "b", "c", "d"), 4, 5, 6, 7};
                    break;
                case "ratio":
                    metric = new Object[] {"ratio", "ratio.name", 12345, newMap("a", "b", "c", "d"), 4, 5};
                    break;
                case "metric":
                    Object[] fields = new Object[2];
                    fields[0] = new Object[] {"count", "SUM", 10};
                    fields[1] = new Object[] {"gauge", "GAUGE", 10.10};
                    metric = new Object[] {"metric", "metric.name", 12345, newMap("a", "b", "c", "d"), fields};
                    break;
                case "histogram1":
                    Object[] hFields = new Object[100];
                    for (int i = 0; i < 100; i++) {
                        hFields[i] = i;
                    }
                    metric = new Object[] {"histogram", "histogram1.name", 12345, newMap("a", "b", "c", "d"), 4, 5, 6,
                        7, 8, 9, hFields};
                    break;
                case "histogram2":
                    Map<String, Integer> hFieldMap = new HashMap<>();
                    hFieldMap.put("1", 1);
                    hFieldMap.put("50", 50);
                    hFieldMap.put("99", 99);
                    metric = new Object[] {"histogram", "histogram2.name", 12345, newMap("a", "b", "c", "d"), 4, 5, 6,
                        7, 8, 9, hFieldMap};
                    break;
            }
        }
        return metric;
    }

    public static Object[] newGenerator(int index, boolean needRandom) throws IOException {
        String type = types[index % types.length];
        Object[] metric;
        if (needRandom) {
            Random rand = new Random();
            Object[] fields = new Object[2];
            fields[0] = new Object[] {"count", "SUM", rand.nextInt(1000)};
            fields[1] = new Object[] {"gauge", "GAUGE", rand.nextDouble()};
            metric = new Object[] {type, "metric.name", 12345, newMap("a", "b", "c", "d"), fields};
        } else {
            Object[] fields = new Object[2];
            fields[0] = new Object[] {"count", "SUM", 10};
            fields[1] = new Object[] {"gauge", "GAUGE", 10.10};
            metric = new Object[] {type, "metric.name", 12345, newMap("a", "b", "c", "d"), fields};
        }
        return metric;
    }

    public static Object[] newGeneratorWithSampling(int index, boolean needRandom) throws IOException {
        String type = types[index % types.length];
        Object[] metric;
        if (needRandom) {
            Random rand = new Random();
            Object[] fields = new Object[2];
            fields[0] = new Object[] {"count", "SUM", rand.nextInt(1000)};
            fields[1] = new Object[] {"gauge", "GAUGE", rand.nextDouble()};
            metric = new Object[] {type, "metric.name", 12345, "sampling", "source", newMap("a", "b", "c", "d"),
                fields};
        } else {
            Object[] fields = new Object[2];
            fields[0] = new Object[] {"count", "SUM", 10};
            fields[1] = new Object[] {"gauge", "GAUGE", 10.10};
            metric = new Object[] {type, "metric.name", 12345, "sampling", "source", newMap("a", "b", "c", "d"),
                fields};
        }
        return metric;
    }

    public static byte[] metricListWithSampling(int size, boolean needRandom) throws IOException {
        Object[] metrics = new Object[size];
        for (int i = 0; i < size; i++) {
            metrics[i] = newGeneratorWithSampling(i, needRandom);
        }
        return JSONUtil.toJsonAsBytes(metrics);
    }

    public static Object[] metricMessageGenerator(boolean needRandom) throws IOException {
        Object[] metricRaw = new Object[11];
        if (needRandom) {
            Random rand = new Random();
            metricRaw[0] = "topic";
            metricRaw[1] = "appId";
            metricRaw[2] = "hostIp";
            metricRaw[3] = "hostName";
            metricRaw[4] = "cluster";
            metricRaw[5] = "ezone";
            metricRaw[6] = "idc";
            metricRaw[7] = "mesosTaskId";
            metricRaw[8] = "eleapposLabel";
            metricRaw[9] = "eleapposSlaveFqdn";
            //for (int i = 0; i < 10; i++) {
            //    metricRaw[i] = String.valueOf(rand.nextInt(1000));
            //}
            Object[] metricList = new Object[rand.nextInt(9) + 1];
            for (int i = 0; i < metricList.length; i++) {
                metricList[i] = metricGenerator(i, true);
            }
            metricRaw[10] = metricList;
        } else {
            metricRaw[0] = "topic";
            metricRaw[1] = "appId";
            metricRaw[2] = "hostIp";
            metricRaw[3] = "hostName";
            metricRaw[4] = "cluster";
            metricRaw[5] = "ezone";
            metricRaw[6] = "idc";
            metricRaw[7] = "mesosTaskId";
            metricRaw[8] = "eleapposLabel";
            metricRaw[9] = "eleapposSlaveFqdn";
            //for (int i = 0; i < 10; i++) {
            //    metricRaw[i] = String.valueOf(i * 100);
            //}
            Object[] metricList = new Object[8];
            for (int i = 0; i < 8; i++) {
                metricList[i] = metricGenerator(i, false);
            }
            metricRaw[10] = metricList;
        }
        return metricRaw;
    }

    public static Object[] newMetricMessageGenerator(boolean needRandom) throws IOException {
        Object[] metricRaw = new Object[11];
        if (needRandom) {
            Random rand = new Random();
            for (int i = 0; i < 10; i++) {
                metricRaw[i] = String.valueOf(rand.nextInt(1000));
            }
            Object[] metricList = new Object[rand.nextInt(4) + 1];
            for (int i = 0; i < metricList.length; i++) {
                metricList[i] = newGenerator(i, true);
            }
            metricRaw[10] = metricList;
        } else {
            for (int i = 0; i < 10; i++) {
                metricRaw[i] = String.valueOf(i * 100);
            }
            Object[] metricList = new Object[3];
            for (int i = 0; i < 3; i++) {
                metricList[i] = newGenerator(i, false);
            }
            metricRaw[10] = metricList;
        }
        return metricRaw;
    }

    public static String combinedMetricMessageGenerator(int size, boolean needRandom) throws IOException {
        Object[] metricMessageList = new Object[size];
        for (int i = 0; i < size; i++) {
            metricMessageList[i] = metricMessageGenerator(needRandom);
        }
        return mapper.writeValueAsString(metricMessageList);
    }

    public static String newCombinedMetricMessageGenerator(int size, boolean needRandom) throws IOException {
        Object[] metricMessageList = new Object[size];
        for (int i = 0; i < size; i++) {
            metricMessageList[i] = newMetricMessageGenerator(needRandom);
        }
        return mapper.writeValueAsString(metricMessageList);
    }

    public static Map<String, String> newMap(String... list) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < list.length - 1; i += 2) {
            map.put(list[i], list[i + 1]);
        }
        return map;
    }
}
