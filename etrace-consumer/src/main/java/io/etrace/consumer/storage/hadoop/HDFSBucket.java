/*
 * Copyright 2019 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.consumer.storage.hadoop;

import io.etrace.agent.Trace;
import io.etrace.common.constant.Constants;
import io.etrace.consumer.storage.Bucket;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class HDFSBucket implements Bucket {
    public final static String MESSAGE_TRACE_PATH = "message_trace";
    private final static Logger LOGGER = LoggerFactory.getLogger(HDFSBucket.class);
    private final AtomicBoolean isClose = new AtomicBoolean(false);
    private final String dataFile;
    private FSDataOutputStream dataStream;
    private final byte compressType;

    public String getDataFile() {
        return dataFile;
    }

    public HDFSBucket(byte compressType, String remotePath, String dataFile) throws IOException {
        this.dataFile = dataFile;
        this.compressType = compressType;
        Path path = HDFSBucket.buildDataFilePath(remotePath, dataFile);

        // todo: remove this
        // 由于日常环境是 admin才有权限WRITE hdfs，因此设置成它
        System.setProperty("HADOOP_USER_NAME", "admin");

        FileSystem fileSystem = FileSystemManager.getFileSystem();
        if (fileSystem.exists(path)) {
            DistributedFileSystem distributedFileSystem = (DistributedFileSystem)fileSystem;
            LOGGER.warn("try to recover file lease for path {}. ", path);
            distributedFileSystem.recoverLease(path);
            //if file not close(maybe client crash), then recover lease
            long now = System.currentTimeMillis();
            while (true) {
                try {
                    long s = System.currentTimeMillis();
                    if (s - now > 5 * 60000) {
                        throw new RuntimeException("recover file lease time out.");
                    }

                    Trace.logEvent("HDFSBucket", "initDataStream", Constants.SUCCESS,
                        String.format("dataFile: %s.", getDataFile()), null);

                    Thread.sleep(5000);
                    boolean isClosed = distributedFileSystem.isFileClosed(path);
                    LOGGER.warn("{} file close status {}.", path, isClosed);
                    dataStream = fileSystem.append(path);//start write data from the end of file
                    break;
                } catch (Throwable e) {
                    LOGGER.warn("append hdfs file error, file name [{}].", path, e);
                    Trace.logError("append hdfs file error, file name: " + path, e);
                    now = System.currentTimeMillis();
                }
            }
        } else {
            dataStream = fileSystem.create(path);
        }
    }

    public static String buildDataFilePathString(String remotePath, String dataFile) {
        if (!remotePath.endsWith(File.separator)) {
            return remotePath + File.separator + MESSAGE_TRACE_PATH + File.separator + dataFile;
        } else {
            return remotePath + MESSAGE_TRACE_PATH + File.separator + dataFile;
        }
    }

    public static Path buildDataFilePath(String remotePath, String dataFile) {
        return new Path(buildDataFilePathString(remotePath, dataFile));
    }

    @Override
    public long writeBlock(byte[] data) throws IOException {
        //save data at remote storage,for compress type(0:gzip,1:snappy)
        dataStream.writeByte(compressType);
        dataStream.writeInt(data.length);
        dataStream.write(data);
        return dataStream.getPos();
    }

    @Override
    public synchronized void close() throws IOException {
        if (isClose.get()) {
            LOGGER.warn("bucket {} is closed.", dataFile);
            return;
        }
        dataStream.close();
        isClose.set(true);
    }

    @Override
    public void flush() throws IOException {
        dataStream.hflush();
    }

    @Override
    public long getLastBlockOffset() throws IOException {
        return dataStream.getPos();
    }

}
