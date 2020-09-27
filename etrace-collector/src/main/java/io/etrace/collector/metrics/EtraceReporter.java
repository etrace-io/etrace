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

package io.etrace.collector.metrics;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.etrace.agent.Trace;
import io.etrace.common.message.metric.impl.DummyTimer;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.core.instrument.step.StepRegistryConfig;
import io.micrometer.core.instrument.util.MeterPartition;
import io.micrometer.core.instrument.util.StringUtils;
import io.micrometer.core.instrument.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EtraceReporter extends StepMeterRegistry {
    public static StepRegistryConfig config;
    public static DefaultClock clock = new DefaultClock();

    static {
        config = new StepRegistryConfig() {
            @Override
            public Duration step() {
                return Duration.ofSeconds(10);
            }

            @Override
            public String prefix() {
                return null;
            }

            @Override
            public String get(String s) {
                return null;
            }
        };
    }

    private final Logger LOGGER = LoggerFactory.getLogger(EtraceReporter.class);

    public EtraceReporter() {
        super(config, clock);
    }

    public void startup() {
        super.start(new ThreadFactoryBuilder().setNameFormat("metrics-reporter").build());
        Metrics.addRegistry(this);
    }

    @Override
    protected TimeUnit getBaseTimeUnit() {
        return TimeUnit.MILLISECONDS;
    }

    @Override
    protected void publish() {
        try {
            for (List<Meter> batch : MeterPartition.partition(this, 100)) {
                for (Meter meter : batch) {
                    Meter.Id id = meter.getId();

                    if (meter instanceof DistributionSummary) {
                        DistributionSummary distribution = (DistributionSummary)meter;
                        HistogramSnapshot snapshot = distribution.takeSnapshot();
                        ValueAtPercentile[] percentiles = snapshot.percentileValues();

                        for (ValueAtPercentile valueAtPercentile : percentiles) {
                            io.etrace.common.message.metric.Gauge traceGauge = Trace.newGauge(
                                id.getName() + "_P" + valueAtPercentile.percentile() * 100);
                            id.getTags().forEach(tag -> traceGauge.addTag(tag.getKey(), tag.getValue()));
                            traceGauge.value(valueAtPercentile.value());
                        }
                    }

                    if (meter instanceof Counter) {
                        Counter counter = (Counter)meter;

                        io.etrace.common.message.metric.Counter traceCounter = Trace.newCounter(id.getName());
                        id.getTags().forEach(tag -> traceCounter.addTag(tag.getKey(), tag.getValue()));
                        traceCounter.value((long)counter.count());
                    }
                }
            }

            clock.add(config.step());

            for (List<Meter> batch : MeterPartition.partition(this, 100)) {
                for (Meter meter : batch) {
                    Meter.Id id = meter.getId();

                    if (meter instanceof DistributionSummary) {
                        DistributionSummary distribution = (DistributionSummary)meter;
                        HistogramSnapshot snapshot = distribution.takeSnapshot();

                        io.etrace.common.message.metric.Timer traceTimer = Trace.newTimer(id.getName());
                        id.getTags().forEach(tag -> traceTimer.addTag(tag.getKey(), tag.getValue()));
                        traceTimer.setUpperEnable(false);
                        //agent-timer不支持写count
                        traceTimer.value((long)snapshot.total());
                        Field countField = traceTimer.getClass().getDeclaredField("count");
                        countField.setAccessible(true);
                        countField.set(traceTimer, snapshot.count());
                    }

                    if (meter instanceof Timer) {
                        Timer timer = (Timer)meter;

                        io.etrace.common.message.metric.Timer traceTimer = Trace.newTimer(id.getName());

                        if (!(traceTimer instanceof DummyTimer)) {
                            id.getTags().forEach(tag -> traceTimer.addTag(tag.getKey(), tag.getValue()));
                            traceTimer.setUpperEnable(false);
                            //agent-timer不支持写count
                            traceTimer.value((long)timer.totalTime(TimeUnit.MILLISECONDS));
                            Field countField = traceTimer.getClass().getDeclaredField("count");
                            countField.setAccessible(true);
                            countField.set(traceTimer, timer.count());

                            Field maxField = traceTimer.getClass().getDeclaredField("max");
                            maxField.setAccessible(true);
                            maxField.set(traceTimer, (long)timer.max(TimeUnit.MILLISECONDS));
                        }
                    }

                    if (meter instanceof Gauge) {
                        Gauge gauge = (Gauge)meter;

                        // todo?  phi?
                        if (StringUtils.isEmpty(meter.getId().getTag("phi"))) {

                            io.etrace.common.message.metric.Gauge traceGauge = Trace.newGauge(id.getName());
                            id.getTags().forEach(tag -> traceGauge.addTag(tag.getKey(), tag.getValue()));
                            traceGauge.value(gauge.value());
                        }
                    }

                }
            }
        } catch (Throwable e) {
            LOGGER.error("", e);
        }
    }

    public static class DefaultClock implements Clock {
        /**
         * has to be non-zero to prevent divide-by-zeroes and other weird math results based on the clock
         */
        private long timeNanos = (long)TimeUtils.millisToUnit(1, TimeUnit.NANOSECONDS);

        @Override
        public long monotonicTime() {
            return timeNanos;
        }

        @Override
        public long wallTime() {
            return TimeUnit.MILLISECONDS.convert(timeNanos, TimeUnit.NANOSECONDS);
        }

        public long add(long amount, TimeUnit unit) {
            timeNanos += unit.toNanos(amount);
            return timeNanos;
        }

        public long add(Duration duration) {
            return add(duration.toNanos(), TimeUnit.NANOSECONDS);
        }

        public long addSeconds(long amount) {
            return add(amount, TimeUnit.SECONDS);
        }
    }
}
