package io.etrace.common.modal.metric;

public enum MetricType {
    Counter("counter", (byte)0x1),
    Gauge("gauge", (byte)0x2),
    Timer("timer", (byte)0x3),
    Payload("payload", (byte)0x4),
    Histogram("histogram", (byte)0x5);

    String name;
    byte type;

    MetricType(String name, byte type) {
        this.name = name;
        this.type = type;
    }

    public byte getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
