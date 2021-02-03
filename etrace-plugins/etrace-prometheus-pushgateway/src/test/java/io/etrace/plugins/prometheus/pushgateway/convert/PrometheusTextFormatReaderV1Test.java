package io.etrace.plugins.prometheus.pushgateway.convert;

import io.etrace.plugins.prometheus.pushgateway.model.PrometheusMetricSampleV1;
import io.etrace.plugins.prometheus.pushgateway.model.PrometheusMetricV1;
import io.prometheus.client.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static io.etrace.plugins.prometheus.pushgateway.constants.PushGatewayConstants.JOB_NAME_KEY;
import static io.etrace.plugins.prometheus.pushgateway.constants.PushGatewayConstants.UNKNOWN_JOB;
import static org.junit.Assert.assertEquals;

// todo: fix this unit test
@Ignore("Unit test fails")
public class PrometheusTextFormatReaderV1Test {

    @Test
    public void parse() throws IOException {
        //  Counter
        String help = "help";
        String name = "test";
        String jobName = "testJobName";

        Map<String, String> tags = new HashMap<>();
        tags.put("testTagName1", "testTagValue");
        tags.put("testTagName2", "testTagValue");

        Map<String, String> labels = new HashMap<>();
        tags.put("testLabelName", "testLabelValue");

        List<String> labelNameList = new ArrayList<>(tags.size());
        List<String> labelValueList = new ArrayList<>(tags.size());
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            labelNameList.add(entry.getKey());
            labelValueList.add(entry.getValue());
        }

        String[] labelNameArray = labelNameList.toArray(new String[] {});
        String[] labelValueArray = labelValueList.toArray(new String[] {});

        double value = 100D;

        // counter
        Counter counter = Counter.build(name, help).labelNames(labelNameArray).create();
        counter.labels(labelValueArray).inc(value);
        List<Collector.MetricFamilySamples> metricFamilySamplesList = counter.collect();
        String json = PrometheusPushgatewayWriter.buildMetricJson(metricFamilySamplesList);
        List<PrometheusMetricV1> prometheusMetricV1List = PrometheusTextFormatReaderV1.parse(json, jobName, null);
        checkData(metricFamilySamplesList, prometheusMetricV1List, null, jobName);

        // gauge
        Gauge gauge = Gauge.build(name, help).labelNames(labelNameArray).create();
        gauge.labels(labelValueArray).inc(value);
        metricFamilySamplesList = gauge.collect();
        json = PrometheusPushgatewayWriter.buildMetricJson(metricFamilySamplesList);
        prometheusMetricV1List = PrometheusTextFormatReaderV1.parse(json, jobName, null);
        checkData(metricFamilySamplesList, prometheusMetricV1List, null, jobName);

        // summaray
        Summary summary = Summary.build(name, help).labelNames(labelNameArray).create();
        summary.labels(labelValueArray).observe(value);
        metricFamilySamplesList = summary.collect();
        json = PrometheusPushgatewayWriter.buildMetricJson(metricFamilySamplesList);
        prometheusMetricV1List = PrometheusTextFormatReaderV1.parse(json, jobName, null);
        checkData(metricFamilySamplesList, prometheusMetricV1List, null, jobName);

        // histogram
        Histogram histogram = Histogram.build(name, help).labelNames(labelNameArray).create();
        histogram.labels(labelValueArray).observe(value);
        metricFamilySamplesList = histogram.collect();
        json = PrometheusPushgatewayWriter.buildMetricJson(metricFamilySamplesList);
        prometheusMetricV1List = PrometheusTextFormatReaderV1.parse(json, jobName, null);
        checkData(metricFamilySamplesList, prometheusMetricV1List, null, jobName);

    }

    private void checkData(List<Collector.MetricFamilySamples> mfsList, List<PrometheusMetricV1> prometheusMetricV1List,
                           Map<String, String> labels, String jobName) {
        assertEquals(mfsList.size(), prometheusMetricV1List.size());
        for (int i = 0; i < mfsList.size(); i++) {
            Collector.MetricFamilySamples mfs = mfsList.get(0);
            PrometheusMetricV1 prometheusMetricV1 = prometheusMetricV1List.get(0);
            List<String> labelNameList = new ArrayList<>();
            List<String> labelValueList = new ArrayList<>();
            if (labels != null) {
                Set<Map.Entry<String, String>> entries = labels.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    labelNameList.add(entry.getKey());
                    labelValueList.add(entry.getValue());
                }
            }
            if (null == jobName) {
                jobName = UNKNOWN_JOB;
            }
            labelNameList.add(JOB_NAME_KEY);
            labelValueList.add(jobName);
            assertEquals(mfs.name, prometheusMetricV1.name);
            assertEquals(mfs.help, prometheusMetricV1.help);
            assertEquals(mfs.type.name(), prometheusMetricV1.type.name());
            assertEquals(mfs.samples.size(), prometheusMetricV1.samples.size());

            for (int j = 0; j < prometheusMetricV1.samples.size(); j++) {
                Collector.MetricFamilySamples.Sample sample = mfs.samples.get(j);
                PrometheusMetricSampleV1 sampleV1 = prometheusMetricV1.samples.get(j);
                assertEquals(sample.name, sampleV1.name);
                assertEquals((Double)sample.value, (Double)sampleV1.value);
                assertEquals(getListSize(sample.labelNames) + labelNameList.size(), getListSize(sampleV1.labelNames));
                assertEquals(getListSize(sample.labelValues) + labelNameList.size(), getListSize(sampleV1.labelValues));
                checkTags(sample.labelNames, sampleV1.labelNames, labelNameList);
                checkTags(sample.labelValues, sampleV1.labelValues, labelValueList);
            }
        }
    }

    private void checkTags(List<String> expected, List<String> actual, List<String> external) {
        if (expected == null) {
            expected = new ArrayList<>();
        }
        ArrayList<String> list = new ArrayList<>(expected);
        list.addAll(external);
        assertEquals(list, actual);

    }

    private int getListSize(List list) {
        if (null == list) {
            return 0;
        }
        return list.size();
    }

    @Test
    public void parseLables() throws UnsupportedEncodingException {
        String jobName = "testJobName";
        Map<String, String> labels = new HashMap<>();
        labels.put("testLabel", "testValue");
        labels.put("testBase64Label", "/testValue");
        String url = PrometheusPushgatewayWriter.buildPushGatewayUrl(jobName, labels);
        Map<String, String> parseLabels = PrometheusTextFormatReaderV1.parseLables(url);
        assertEquals(parseLabels, labels);
    }

    @Test
    public void decodeBase64() {
        String value = "testValue";
        assertEquals(value, PrometheusTextFormatReaderV1.decodeBase64(PrometheusPushgatewayWriter.base64url(value)));

        value = "/testValue";
        assertEquals(value, PrometheusTextFormatReaderV1.decodeBase64(PrometheusPushgatewayWriter.base64url(value)));
    }
}