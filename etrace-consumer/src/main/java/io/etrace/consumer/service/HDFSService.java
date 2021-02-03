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

package io.etrace.consumer.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import io.etrace.common.message.trace.CallStackV1;
import io.etrace.common.message.trace.codec.JSONCodecV1;
import io.etrace.consumer.config.ConsumerProperties;
import io.etrace.consumer.model.BlockIndex;
import io.etrace.consumer.storage.hadoop.FileSystemManager;
import io.etrace.consumer.storage.hadoop.HDFSBucket;
import io.etrace.consumer.storage.hadoop.PathBuilder;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xerial.snappy.SnappyInputStream;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

@Service
public class HDFSService {
    private final static Logger LOGGER = LoggerFactory.getLogger(HDFSService.class);

    protected String stackPath;
    protected Cache<String, DataReader> dataReaderCache;
    @Autowired
    private ConsumerProperties consumerProperties;

    @PostConstruct
    public void startup() {
        stackPath = consumerProperties.getHdfs().getPath() + File.separator + HDFSBucket.MESSAGE_TRACE_PATH
            + File.separator;
        dataReaderCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .removalListener((RemovalListener<String, DataReader>)removalNotification -> {
                if (removalNotification.getValue() != null) {
                    try {
                        removalNotification.getValue().close();
                    } catch (IOException e) {
                        LOGGER.error("close hdfs data file error, file is {}", removalNotification.getKey(), e);
                    }
                }
            })
            .maximumSize(512).build();
        LOGGER.info("hdfs start success!");

    }

    public CallStackV1 findMessage(BlockIndex blockIndex) {
        try {
            String dataFile = PathBuilder.buildMessagePath(blockIndex.getIndex() + "-" + blockIndex.getIp(),
                blockIndex.getHour());
            DataReader dataReader = dataReaderCache.getIfPresent(dataFile);

            if (null == dataReader) {
                String dataFilePath = stackPath + dataFile;
                dataReader = new DataReader(dataFilePath);
                dataReaderCache.put(dataFile, dataReader);
            }
            try {
                if (blockIndex.getBlockId() > 0) {
                    byte[] data = dataReader.readMessage(blockIndex.getBlockId(), blockIndex.getOffset());
                    if (data != null) {
                        return JSONCodecV1.decodeToV1FromArrayFormat(data);
                    }
                }
            } catch (Throwable e) {
                LOGGER.error("cannot create data reader for file {}.", dataFile, e);
            }
        } catch (Throwable e) {
            LOGGER.error("callstack find message error:", e);
        }
        return null;
    }

    public static class DataReader {
        private final FSDataInputStream dataFile;

        public DataReader(String dataFileFile) throws IOException {
            FileSystem fs = FileSystemManager.getFileSystem();
            Path basePath = new Path(dataFileFile);

            dataFile = fs.open(new Path(basePath, dataFileFile));
        }

        void close() throws IOException {
            synchronized (dataFile) {
                dataFile.close();
            }
        }

        public byte[] readMessage(long blockOffset, int messageOffset) throws IOException {
            byte[] buf;
            byte compressType;
            synchronized (dataFile) {
                dataFile.seek(blockOffset);
                compressType = dataFile.readByte();
                buf = new byte[dataFile.readInt()];
                dataFile.readFully(buf);
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(buf);
            DataInputStream in = null;
            if (compressType == 0) {
                in = new DataInputStream(new GZIPInputStream(bais));
            } else if (compressType == 1) {
                in = new DataInputStream(new SnappyInputStream(bais));
            }
            if (in == null) {
                return null;
            }

            try {
                in.skip(messageOffset);
                int len = in.readInt();
                byte[] data = new byte[len];

                in.readFully(data);
                return data;
            } finally {
                try {
                    in.close();
                } catch (Exception e) {
                    // ignore it
                }
            }
        }
    }
}
