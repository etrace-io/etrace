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

package io.etrace.consumer.service;

import com.google.common.collect.Lists;
import io.etrace.common.message.metric.field.MetricType;
import io.etrace.common.util.JSONUtil;
import io.etrace.common.util.TimeHelper;
import io.etrace.consumer.model.SamplingResponse;
import io.etrace.consumer.storage.hbase.IHBaseClientFactory;
import io.etrace.consumer.storage.hbase.IHBaseTableNameFactory;
import io.etrace.consumer.storage.hbase.impl.MetricImpl;
import io.etrace.consumer.util.RegexpBuilder;
import io.etrace.consumer.util.RowKeyUtil;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;

@Service
public class HBaseSamplingDao {
    private final Logger LOGGER = LoggerFactory.getLogger(HBaseStackDao.class);

    @Autowired
    private IHBaseClientFactory IHBaseClientFactory;
    @Autowired
    private MetricImpl metricImpl;
    @Autowired
    private IHBaseTableNameFactory IHBaseTableNameFactory;

    public List<SamplingResponse> sampling(String metricType, String name, long timestamp, int interval,
                                           Map<String, Object> tags) throws IOException {
        List<SamplingResponse> samplingList = newArrayList();
        //special
        if (name.endsWith(".jvm_thread")) {
            if (tags.containsKey("name")) {
                tags.remove("name");
            }
        }

        int day = TimeHelper.getDay(timestamp);
        HTable table = IHBaseClientFactory.getTableByPhysicalName(
            IHBaseTableNameFactory.getPhysicalTableNameByLogicalTableName(metricImpl.getLogicalTableName(), day));

        ResultScanner resultScanner = null;
        try {
            MetricType type = MetricType.fromIdentifier(metricType);
            short shard = IHBaseClientFactory.getShardIdByPhysicalTableName(table.getName().getNameAsString(),
                metricImpl.metricHashcode(name));
            byte[] prefixKey = RowKeyUtil.build(shard, type.code(), name);
            byte[] startKey = RowKeyUtil.build(shard, type.code(), name, timestamp);
            byte[] endKey;
            if (interval > 0) {
                endKey = RowKeyUtil.build(shard, type.code(), name, timestamp + interval - 1);
            } else {
                endKey = new byte[startKey.length + 1];
                System.arraycopy(startKey, 0, endKey, 0, startKey.length);
                endKey[endKey.length - 1] = (byte)Integer.MAX_VALUE;
            }

            FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
            //pageFilter
            PageFilter p = new PageFilter(100);
            filterList.addFilter(p);
            //regexpFilter
            RegexpBuilder rb = new RegexpBuilder();

            //todo 严格来说skipLong是不对的，只有当 hasMaxField(MetricType) 为true时，才需要.
            rb.append(prefixKey).skipLong();
            buildTagsRegFilter(tags, rb);

            RegexStringComparator comp = new RegexStringComparator(rb.buildRegexp());
            comp.setCharset(Charset.forName("ISO-8859-1"));
            RowFilter filter = new RowFilter(CompareFilter.CompareOp.EQUAL, comp);
            filterList.addFilter(filter);

            Scan scan = new Scan();
            //scan.setCaching(100);
            scan.setStartRow(startKey);
            scan.setStopRow(endKey);
            scan.setFilter(filterList);

            resultScanner = table.getScanner(scan);
            try {
                for (Result r : resultScanner) {
                    NavigableMap<byte[], byte[]> familyMap = r.getFamilyMap(metricImpl.getColumnFamily());
                    for (Map.Entry<byte[], byte[]> qualifier : familyMap.entrySet()) {
                        SamplingResponse bean = new SamplingResponse();
                        bean.setSamplings(JSONUtil.toObject(qualifier.getValue(), Map.class));

                        SamplingResponse samplingResponse = getSamplingResp(bean.getSamplings(), samplingList);

                        if (null == samplingResponse) {
                            samplingList.add(bean);
                        }
                        if (metricImpl.hasMaxField(type)) {
                            byte[] rowKey = r.getRow();
                            long value = Long.MAX_VALUE - Bytes.toLong(rowKey, startKey.length);
                            if (null != samplingResponse) {
                                if (samplingResponse.getMaxValue().compareTo(value) < 0) {
                                    samplingResponse.setMaxValue(value);
                                }
                            } else {
                                bean.setMaxValue(value);
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {
                LOGGER.error("", ignored);
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            IHBaseClientFactory.closeHTable(table);
            IHBaseClientFactory.closeScanner(resultScanner);
        }
        return samplingList;
    }

    private void buildTagsRegFilter(Map<String, Object> tags, RegexpBuilder rb) {
        if (Objects.isNull(tags)) {
            return;
        }
        Map<Integer, List<Integer>> tagHashcode = new TreeMap<>();
        tags.forEach((key, v) -> {
            if (v instanceof String) {
                tagHashcode.put(key.hashCode(), Lists.newArrayList(v.hashCode()));
                return;
            }
            if (!(v instanceof Collection)) {
                return;
            }
            List<Integer> list = new ArrayList<>();
            ((Collection)v).stream().filter(e -> e instanceof String).forEach(e -> list.add(e.hashCode()));
            tagHashcode.put(key.hashCode(), list);
        });
        for (Map.Entry<Integer, List<Integer>> taghash : tagHashcode.entrySet()) {
            rb.append(taghash.getKey()).appendMultiValue(taghash.getValue().stream().mapToInt(i -> i).toArray())
                .skipLong();
        }
    }

    private SamplingResponse getSamplingResp(Map<String, String> obj, List<SamplingResponse> samplingResponses) {
        for (SamplingResponse sampling : samplingResponses) {
            if (sampling.getSamplings().equals(obj)) {
                return sampling;
            }
        }
        return null;
    }
}
