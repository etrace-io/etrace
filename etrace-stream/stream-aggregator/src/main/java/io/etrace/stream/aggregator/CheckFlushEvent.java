package io.etrace.stream.aggregator;

public class CheckFlushEvent {
    // signal for esper to trigger checkFlush
    // common.epl 有epl计算checkFlushEvent 次数
    // 保证esper check flush 可以传递到kafka sink
}
