package io.etrace.common.compression;

import org.xerial.snappy.SnappyOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SnappyCompressor {
    FixedByteArrayOutputStream baos;
    OutputStream out;
    int blockSize;
    AtomicBoolean dirty = new AtomicBoolean(false);
    private long lastFlushTime;

    public SnappyCompressor(int blockSize) {
        this.baos = new FixedByteArrayOutputStream(blockSize);
        this.out = new SnappyOutputStream(this.baos);
    }

    public int size() {
        return this.blockSize;
    }

    public byte[] flush() throws IOException {
        if (dirty.get()) {
            out.close();
            baos.flush();
            byte[] data = baos.toByteArray();
            blockSize = 0;
            lastFlushTime = System.currentTimeMillis();
            baos.reset();
            out = new SnappyOutputStream(baos);
            dirty.set(false);
            return data;
        }
        return null;
    }

    public long lastFlushTime() {
        return lastFlushTime;
    }
}
