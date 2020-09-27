package io.etrace.common.compression;

import io.etrace.common.util.Bytes;

import java.io.IOException;

/**
 * not thread safe
 */
public class MetricCompressor extends Compressor {

    public MetricCompressor() {
        super(2 << 13);
    }

    public int store(byte[] data) throws IOException {
        dirty.set(true);
        this.blockSize += data.length + 4;
        out.write(Bytes.toBytes(data.length));
        out.write(data);
        return this.blockSize;
    }
}
