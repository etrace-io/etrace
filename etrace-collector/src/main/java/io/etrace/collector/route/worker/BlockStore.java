package io.etrace.collector.route.worker;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.etrace.collector.util.Bytes;
import io.etrace.common.bucket.CompressType;
import org.xerial.snappy.SnappyOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.collect.Lists.newArrayList;

public class BlockStore {
    private OutputStream out;
    private ByteArrayOutputStream buf;
    private AtomicBoolean dirty = new AtomicBoolean();
    private int blockSize;
    private CompressType compressType;
    private List<Integer> offsets;
    private Map<Integer, String> instances = Maps.newHashMap();

    public BlockStore(CompressType compressType) {
        this.compressType = compressType;
        this.offsets = newArrayList();
        buf = new ByteArrayOutputStream(16384);
        if (compressType == CompressType.snappy) {
            out = new SnappyOutputStream(buf);
        } else {
            out = buf;
        }
    }

    public int store(byte[] data, String instance) throws IOException {
        dirty.set(true);
        int offset = this.blockSize;
        this.blockSize += data.length + 4;
        out.write(Bytes.toBytes(data.length));
        out.write(data);
        offsets.add(offset);

        if (!Strings.isNullOrEmpty(instance)) {
            instances.put(offsets.size() - 1, instance);
        }
        return blockSize;
    }

    public List<Integer> getOffsets() {
        return offsets;
    }

    public Map<Integer, String> getInstances() {
        return instances;
    }

    public byte[] flushBlock() throws IOException {
        if (dirty.get()) {
            out.close();
            byte[] data = buf.toByteArray();
            blockSize = 0;
            buf.reset();
            if (compressType == CompressType.snappy) {
                out = new SnappyOutputStream(buf);
            } else {
                out = buf;
            }
            dirty.set(false);
            return data;
        }
        return null;
    }

    public void clear() {
        offsets.clear();
        instances.clear();
    }

    public int getBlockSize() {
        return blockSize;
    }
}
