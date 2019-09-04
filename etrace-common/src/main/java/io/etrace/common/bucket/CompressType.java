package io.etrace.common.bucket;

/**
 * @author jie.huang
 *         Date: 16/9/20
 *         Time: 上午10:25
 */
public enum CompressType {
    none((byte) -1),
    gzip((byte) 0),
    snappy((byte) 1);

    private final byte code;

    CompressType(byte code) {
        this.code = code;
    }

    public byte code() {
        return code;
    }

    public static CompressType findByCode(int code) {
        switch (code) {
            case 0:
                return gzip;
            case 1:
                return snappy;
            default:
                return none;
        }
    }
}
