package io.etrace.common.compression;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.etrace.common.util.Bytes;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TraceCompressor extends Compressor {
    private List<Integer> offsets = Lists.newArrayList();
    private Map<Integer, String> instances = Maps.newHashMap();

    public TraceCompressor() {
        super(2 << 15);
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
        return this.blockSize;
    }

    public List<Integer> getOffsets() {
        return offsets;
    }

    public Map<Integer, String> getInstances() {
        return instances;
    }

    public void clear() {
        offsets.clear();
        instances.clear();
    }
}
