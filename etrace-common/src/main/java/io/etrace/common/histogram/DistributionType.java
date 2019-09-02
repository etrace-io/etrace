package io.etrace.common.histogram;

public enum DistributionType {
    Percentile("percentile", (byte)0x1);

    private final String type;
    private final byte code;

    DistributionType(String type, byte code) {
        this.type = type;
        this.code = code;
    }

    public static DistributionType findByType(String type) {
        if (type == null) {
            throw new RuntimeException("Metric type is null");
        }
        switch (type.toLowerCase()) {
            case "percentile":
                return Percentile;
            default:
                throw new RuntimeException("Unknown distribution analyzer type for " + type);
        }
    }

    public static DistributionType findByCode(int code) {
        switch (code) {
            case ((byte)0x1):
                return Percentile;
            default:
                throw new RuntimeException("Unknown distribution analyzer code for " + code);
        }
    }

    public String type() {
        return type;
    }

    public byte code() {
        return code;
    }
}
