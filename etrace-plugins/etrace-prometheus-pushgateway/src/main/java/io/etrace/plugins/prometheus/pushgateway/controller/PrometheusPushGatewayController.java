package io.etrace.plugins.prometheus.pushgateway.controller;

import io.etrace.agent.Trace;
import io.etrace.plugins.prometheus.pushgateway.convert.EtraceHelper;
import io.etrace.plugins.prometheus.pushgateway.convert.PrometheusTextFormatReaderV1;
import io.etrace.plugins.prometheus.pushgateway.model.EtraceExtendInfo;
import io.etrace.plugins.prometheus.pushgateway.model.PrometheusMetricV1;
import io.etrace.plugins.prometheus.pushgateway.sender.EtracePrometheusDataSender;
import io.etrace.plugins.prometheus.pushgateway.util.HttpServletRequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@RestController
public class PrometheusPushGatewayController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusPushGatewayController.class);

    @Autowired
    private EtracePrometheusDataSender prometheusDataSender;

    public PrometheusPushGatewayController() {

    }

    @RequestMapping(value = "/metrics/**", method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity postMetric(HttpServletRequest request, @RequestBody(required = false) String json) throws IOException {
        if (null == json) {
            return ResponseEntity.accepted().build();
        }
        String jobName = PrometheusTextFormatReaderV1.parseJobName(request.getRequestURI());
        String remoteHost = request.getRemoteHost();
        String remoteIp = HttpServletRequestUtil.getRemoteIp(request);
        Map<String, String> groupKeys = PrometheusTextFormatReaderV1.parseLables(request.getRequestURI());
        EtraceExtendInfo etraceExtendInfo = EtraceHelper.buildEtraceExtendInfo(remoteHost, remoteIp);
        List<PrometheusMetricV1> prometheusMetricV1List;
        try {
            prometheusMetricV1List = PrometheusTextFormatReaderV1.parse(json, jobName, groupKeys);
        } catch (Exception e) {
            LOGGER.error("parse Prometheus data error,origin data:{},error:{}", json, e);
            return ResponseEntity.badRequest().build();
        }
        try {
            boolean success = prometheusDataSender.send(prometheusMetricV1List, etraceExtendInfo);
            Trace.newCounter("prometheus.data.send").addTag("result", String.valueOf(success)).once();
            if (success) {
                return ResponseEntity.accepted().build();
            }
        } catch (Exception e) {
            Trace.newCounter("prometheus.data.send").addTag("result", "Exception").once();
            LOGGER.error("send data error", e);
        }
        return ResponseEntity.badRequest().build();
    }


}
