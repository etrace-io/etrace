package io.etrace.common.compression;

import io.etrace.common.util.Bytes;
import lombok.Getter;

import java.io.IOException;

@Getter
public class TraceCompressor extends SnappyCompressor {

    public TraceCompressor() {
        super(2 << 15);
    }

    public int store(byte[] data) throws IOException {
        dirty.set(true);
        this.blockSize += data.length + 4;
        out.write(Bytes.toBytes(data.length));
        out.write(data);
        return this.blockSize;
    }

    public void clear() {
    }
}
