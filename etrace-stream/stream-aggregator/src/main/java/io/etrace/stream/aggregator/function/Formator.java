package io.etrace.stream.aggregator.function;

import io.etrace.common.message.metric.field.AggregateType;
import io.etrace.common.message.metric.field.Field;
import io.etrace.common.util.TimeHelper;
import io.etrace.stream.aggregator.annotation.UserDefineFunction;

public class Formator {

    @UserDefineFunction(name = "trunc_min")
    public static long timeMinute(long timestamp) {
        return TimeHelper.truncateByMinute(timestamp);
    }

    @UserDefineFunction(name = "trunc_sec")
    public static long truncateBySecond(long timestamp, int second) {
        return timestamp / (second * 1000) * (second * 1000);
    }

    @UserDefineFunction(name = "f_sum")
    public static Field sumField(double value) {
        return new Field(AggregateType.SUM, value);
    }

    @UserDefineFunction(name = "f_min")
    public static Field minField(double value) {
        return new Field(AggregateType.MIN, value);
    }

    @UserDefineFunction(name = "f_max")
    public static Field maxField(double value) {
        return new Field(AggregateType.MAX, value);
    }

    @UserDefineFunction(name = "f_gauge")
    public static Field gaugeField(double value) {
        return new Field(AggregateType.GAUGE, value);
    }
}
