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

package io.etrace.consumer.controller;

import com.google.common.base.Strings;
import io.etrace.consumer.exception.SamplingException;
import io.etrace.consumer.model.SamplingRequest;
import io.etrace.consumer.model.SamplingResponse;
import io.etrace.consumer.service.HBaseSamplingDao;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SamplingController {

    @Autowired
    private HBaseSamplingDao hBaseSamplingDao;

    @PostMapping("/sampling")
    public List<SamplingResult> querySampling(@RequestBody SamplingRequest sampling) throws IOException {
        if (Strings.isNullOrEmpty(sampling.getMetricName()) || sampling.getTimestamp() == 0) {
            throw new SamplingException("wrong request. no metric name or no timestamp");
        }
        List<SamplingResponse> samplings = hBaseSamplingDao.sampling(sampling.getMetricType(), sampling.getMetricName(),
            sampling.getTimestamp(), sampling.getInterval(), sampling.getTags());

        if (samplings.size() > 0) {
            return samplings.stream()
                .map(SamplingResult::parse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
        throw new SamplingException("not found sampling");
    }

    @Data
    public static class SamplingResult {
        String value;
        String max;
        Long maxValue;

        public static SamplingResult parse(SamplingResponse samplingBean) {
            if (samplingBean.getSamplings() == null ||
                (samplingBean.getSamplings().getOrDefault("value", "null").equals("null")
                    && samplingBean.getSamplings().getOrDefault("max", "null").equals("null"))) {
                return null;
            } else {
                SamplingResult result = new SamplingResult();
                result.value = samplingBean.getSamplings().get("value");
                result.max = samplingBean.getSamplings().get("max");

                if (samplingBean.getMaxValue() != null) {
                    result.maxValue = samplingBean.getMaxValue();
                }
                return result;
            }
        }
    }

}
