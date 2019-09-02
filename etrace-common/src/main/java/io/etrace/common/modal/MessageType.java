package io.etrace.common.modal;

public enum MessageType {
    EVENT((byte)1),
    TRANSACTION((byte)2),
    HEARTBEAT((byte)3),
    UNKNOWN((byte)100);

    private byte code;

    MessageType(byte code) {
        this.code = code;
    }

    public static MessageType findByCode(byte code) {
        switch (code) {
            case 1:
                return EVENT;
            case 2:
                return TRANSACTION;
            case 3:
                return HEARTBEAT;
            default:
                return UNKNOWN;
        }
    }

    public byte code() {
        return code;
    }
}
