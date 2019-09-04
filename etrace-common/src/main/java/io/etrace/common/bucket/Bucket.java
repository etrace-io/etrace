package io.etrace.common.bucket;

import java.io.IOException;

public interface Bucket {

    long getLastBlockOffset() throws IOException;

    void close() throws IOException;

    void flush() throws IOException;

    long writeBlock(byte[] data) throws IOException;
}
