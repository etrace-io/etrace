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

package io.etrace.plugins.datasource.prometheus.autoconfigure;

import io.etrace.common.datasource.MetricDatasourceService;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.step.StepRegistryConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnMissingBean(MetricDatasourceService.class)
public class PrometheusStepMeterAutoConfiguration {

    @Bean
    public StepMeterRegistry stepMeterRegistry() {
        return new StepMeterRegistry(new StepRegistryConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(5);
            }

            @Override
            public String prefix() {
                return null;
            }

            @Override
            public String get(String key) {
                return null;
            }
        }, Clock.SYSTEM) {
            @Override
            protected TimeUnit getBaseTimeUnit() {
                return TimeUnit.SECONDS;
            }

            @Override
            protected void publish() {

            }
        };
    }
}
