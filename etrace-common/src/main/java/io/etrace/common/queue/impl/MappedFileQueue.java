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

package io.etrace.common.queue.impl;

import com.google.common.base.Strings;
import io.etrace.common.queue.PersistentQueue;
import io.etrace.common.queue.QueueCodec;
import io.etrace.common.queue.QueueConfig;
import io.etrace.common.util.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.ch.DirectBuffer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.nio.channels.FileChannel.MapMode.READ_WRITE;

/**
 * This is a memory mapped persistent queue where the objects are produced and consumed in a FIFO basis
 * <p>
 * for persistent queue, two memory mapped byte buffers will be created both for reading and writing
 * <p>
 * The data packet format will be [1byte][4bytes][data bytes] where first header is to mention whether the packet is
 * already consumed or not. second header is 4 bytes size, specifying the length of the packet, and the rest is the
 * actual data.
 * <p>
 * The backlog is also taken care.
 * <p>
 * The index file is used for keeping read and write file pointers.
 */
public class MappedFileQueue<T> implements PersistentQueue<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MappedFileQueue.class);
    private static final int PAGE_SIZE_MB = 128;
    private static final int PAGE_SIZE = 1024 * 1024 * PAGE_SIZE_MB; // 128 MB Page Size
    private static final int OBJECT_SIZE_LIMIT = 1024 * 1024 * 32; // 32 MB Object Size limit
    private static final String DATA_FILE_SUFFIX = ".dat";
    final private int headerLength = 5;
    final private byte READ = (byte)0;
    final private byte NOT_READ = (byte)1;
    final private byte MM_EOF = (byte)2;
    final private byte RAW_BYTES = (byte)3;
    final private ByteBuffer header = ByteBuffer.allocate(headerLength);
    // 1 byte for status of the message, 4 bytes length of the payload
    final private int endingLength = 5;
    private RandomAccessFile readDataFile; //random access file for data
    private RandomAccessFile writeDataFile; //random access file for data
    private FileChannel readDataChannel; // channel for read data
    private FileChannel writeDataChannel; // channel for write data
    private MappedByteBuffer readMbb; // buffer used to read;
    private MappedByteBuffer writeMbb; // buffer used to write
    private long readFileIndex = 0; // read index file
    private long writeFileIndex = 0; // write index file
    private long maxFileSize;
    private String fileNamePrefix; // persistence file name
    private File queueDir; // queue directory

    private TreeMap<Long, File> files = new TreeMap<>();
    private BlockingQueue<File> toDeleteFiles = new LinkedBlockingQueue<>();
    private File currentReadingFile;
    private File currentWritingFile;
    private QueueCodec codec;
    private volatile boolean active;

    public MappedFileQueue(String queueName, QueueConfig config) throws RuntimeException {
        String queuePath = config.getRootPath() + File.separator + config.getName() + File.separator + "thread-"
            + config.getIdx();

        this.maxFileSize = config.getMaxFileSize() * 1024 * 1024 * 1024;
        if (Strings.isNullOrEmpty(queuePath)) {
            throw new IllegalArgumentException("QueuePath[" + queuePath + "] is empty");
        }
        if (Strings.isNullOrEmpty(queueName)) {
            throw new IllegalArgumentException("Name[" + queueName + "] is empty");
        }
        String qDir = queuePath;
        if (!qDir.endsWith("/")) {
            qDir += File.separator;
        }
        qDir += queueName;
        queueDir = new File(qDir);
        if (!queueDir.exists()) {
            queueDir.mkdirs();
        }
        this.fileNamePrefix = queueDir.getPath();
        if (!this.fileNamePrefix.endsWith("/")) {
            this.fileNamePrefix += File.separator;
        }
        this.fileNamePrefix += "data";
        if (maxFileSize < 0 || maxFileSize % PAGE_SIZE_MB != 0) {
            throw new IllegalArgumentException(
                "max file size must be positive and a multiple of " + PAGE_SIZE_MB + " MB");
        }
        try {
            this.init(config.getName(), config.getIdx());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void unMap(MappedByteBuffer buffer) {
        if (buffer == null) {
            return;
        }
        sun.misc.Cleaner cleaner = ((DirectBuffer)buffer).cleaner();
        if (cleaner != null) {
            cleaner.clean();
        }
    }

    private static void closeResource(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (Exception ignore) {            /* Do Nothing */
        }
    }

    private void init(String name, int idx) throws IOException {
        initFiles();

        this.initReadBuffer();
        this.initWriteBuffer();
        this.active = true;
        Thread thread = new Thread(new FileCleanTask());
        thread.setName("file-clean-" + name + "-" + idx);
        thread.start();
    }

    private void initFiles() throws IOException {
        File[] queueFiles = queueDir.listFiles((dir, name) -> {
            return name.endsWith(DATA_FILE_SUFFIX);
        });
        if (queueFiles != null) {
            for (File queueFile : queueFiles) {
                String fileName = queueFile.getName();
                int beginIndex = fileName.lastIndexOf('-') + 1;
                int endIndex = fileName.lastIndexOf(DATA_FILE_SUFFIX);
                String sIndex = fileName.substring(beginIndex, endIndex);
                long index = Long.parseLong(sIndex);
                files.put(index, queueFile);
            }
        }
        if (!files.isEmpty()) {
            writeFileIndex = files.lastKey();
            files.remove(writeFileIndex);
        }
        if (!files.isEmpty()) {
            readFileIndex = files.lastKey();
            files.remove(readFileIndex);
        } else {
            readFileIndex = writeFileIndex;
        }
    }

    private void initReadBuffer() throws IOException {
        currentReadingFile = new File(this.fileNamePrefix + "-" + readFileIndex + DATA_FILE_SUFFIX);
        readDataFile = new RandomAccessFile(currentReadingFile, "rw");
        readDataChannel = readDataFile.getChannel();
        readMbb = readDataChannel.map(READ_WRITE, 0, PAGE_SIZE); // create the read buffer with readPosition 0 initially
        int position = readMbb.position();
        byte active = readMbb.get(); // first byte to see whether the message is already read or not
        int length = readMbb.getInt(); // next four bytes to see the data length

        while (active == READ && length > 0) { // message is non active means, its read, so skipping it
            readMbb.position(position + headerLength + length); // skipping the read bytes
            position = readMbb.position();
            active = readMbb.get();
            length = readMbb.getInt();
        }
        readMbb.position(position);
    }

    private void initWriteBuffer() throws IOException {
        currentWritingFile = new File(this.fileNamePrefix + "-" + writeFileIndex + DATA_FILE_SUFFIX);
        writeDataFile = new RandomAccessFile(currentWritingFile, "rw");
        writeDataChannel = writeDataFile.getChannel();
        writeMbb = writeDataChannel.map(READ_WRITE, 0,
            PAGE_SIZE); // start the write buffer with writePosition 0 initially
        int position = writeMbb.position();
        //read header
        byte active = writeMbb.get();
        int length = writeMbb.getInt();
        while (length > 0) { // message is there, so skip it, keep doing until u get the end
            writeMbb.position(position + headerLength + length);
            position = writeMbb.position();
            //read next head
            active = writeMbb.get();
            length = writeMbb.getInt();
        }
        writeMbb.position(position);
    }

    @Override
    public void setQueueCodec(QueueCodec codec) {
        this.codec = codec;
    }

    @Override
    public boolean produce(T data) {
        byte[] bytes = codec.encode(data);
        return !(bytes != null && bytes.length > 0) || produceToDiskFile(bytes);
    }

    @Override
    public T consume() {
        byte[] data = consumeFromDiskFile();
        if (data != null && data.length > 0) {
            return (T)codec.decode(data);
        }
        return null;
    }

    private synchronized byte[] consumeFromDiskFile() {
        try {
            if (null == readMbb) {
                return null;
            }
            int currentPosition = readMbb.position();
            byte active = readMbb.get();
            int length;
            // end of the file
            if (active == MM_EOF) {
                finishCurrentFile();
                setCurrentReadingFile();
                if (currentReadingFile == null) {
                    return null;
                }
                currentPosition = readMbb.position();
                active = readMbb.get();
                length = readMbb.getInt();
                while (active == READ && length > 0) { // message is non active means, its read, so skipping it
                    readMbb.position(currentPosition + headerLength + length); // skipping the read bytes
                    currentPosition = readMbb.position();
                    active = readMbb.get();
                    length = readMbb.getInt();
                }
            } else {
                length = readMbb.getInt();
            }

            if (active != RAW_BYTES || length <= 0) {
                readMbb.position(currentPosition);
                if (writeFileIndex != readFileIndex) {
                    LOGGER.error("error queue file,delete this file:" + currentReadingFile.getName() + ",position:"
                        + currentPosition);
                    finishCurrentFile();//finish current file and del
                    setCurrentReadingFile();//set next file
                }
                return null; // the queue is empty
            }
            byte[] bytes = new byte[length];
            readMbb.get(bytes);
            readMbb.put(currentPosition, READ); // making it not active (deleted)
            return bytes;
        } catch (Throwable e) {
            LOGGER.error("Issue in reading the persistent queue : ", e);
        }
        return null;
    }

    private void setCurrentReadingFile() throws IOException {
        if (readFileIndex == writeFileIndex) {
            return;
        }
        if (!files.isEmpty()) {
            readFileIndex = files.lastKey();
            files.remove(readFileIndex);
        } else {
            readFileIndex = writeFileIndex;
        }
        currentReadingFile = new File(this.fileNamePrefix + "-" + readFileIndex + DATA_FILE_SUFFIX);
        readDataFile = new RandomAccessFile(currentReadingFile, "rw");
        readDataChannel = readDataFile.getChannel();
        readMbb = readDataChannel.map(READ_WRITE, 0, PAGE_SIZE);
    }

    private void finishCurrentFile() {
        unMap(readMbb);
        closeResource(readDataChannel);
        closeResource(readDataFile);
        toDeleteFiles.add(currentReadingFile);
        currentReadingFile = null;
    }

    private synchronized boolean produceToDiskFile(byte[] bytes) {
        try {
            int length = bytes.length;
            if (length == 0) {
                LOGGER.warn("Issue in dumping the object with zero byte into persistent queue");
                return false;
            }
            if (length > OBJECT_SIZE_LIMIT) {
                LOGGER.warn(
                    "Issue in dumping the object into persistent queue, object size " + length + " exceeds limit "
                        + OBJECT_SIZE_LIMIT + " MB");
                return false;
            }

            //prepare the header
            header.clear();
            header.put(RAW_BYTES);
            header.putInt(length);
            header.flip();

            if (writeMbb.remaining() < headerLength + length
                + endingLength) { // check weather current buffer is end buffer, otherwise we need to change the buffer
                writeMbb.put(MM_EOF); // the end
                //                writeMbb.force();
                unMap(writeMbb);
                closeResource(writeDataChannel);
                closeResource(writeDataFile);
                if (writeFileIndex != readFileIndex) {
                    files.put(writeFileIndex, currentWritingFile);
                }
                writeFileIndex++;
                currentWritingFile = new File(this.fileNamePrefix + "-" + writeFileIndex + DATA_FILE_SUFFIX);
                writeDataFile = new RandomAccessFile(currentWritingFile, "rw");
                writeDataChannel = writeDataFile.getChannel();
                writeMbb = writeDataChannel.map(READ_WRITE, 0,
                    PAGE_SIZE); // start the write buffer with writePosition 0 initially
            }

            writeMbb.put(header); // write header
            writeMbb.put(bytes);
            return true;
        } catch (Throwable e) {
            LOGGER.error("Issue in dumping the object into persistent " + e);
            return false;
        }
    }

    @Override
    public long remainingCapacity() {
        return Integer.MAX_VALUE;//fake
    }

    @Override
    public long capacity() {
        return maxFileSize;//fake
    }

    @Override
    public long usedSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        if (null == readMbb || null == writeMbb) {
            return true;
        }
        return this.readFileIndex == this.writeFileIndex && readMbb.position() == writeMbb.position();
    }

    @Override
    public void shutdown() {

        if (writeMbb != null) {
            writeMbb.force();
            unMap(writeMbb);
        }
        if (readMbb != null) {
            readMbb.force();
            unMap(readMbb);
        }

        closeResource(readDataChannel);
        closeResource(readDataFile);
        closeResource(writeDataChannel);
        closeResource(writeDataFile);
        writeMbb = null;
        readMbb = null;
        active = false;
    }

    @Override
    public long getOverflowCount() {
        return 0;
    }

    @Override
    public int getBackFileSize() {
        return 0;
    }

    /**
     * Periodically delete old used file which are not in current read/write window.
     */
    class FileCleanTask implements Runnable {
        @Override
        public void run() {
            while (active) {
                File file = null;
                try {
                    file = toDeleteFiles.take();
                } catch (InterruptedException e) {
                    LOGGER.warn("File clean task thread interrupted, ", e);
                }
                if (file != null) {
                    file.delete();
                    LOGGER.info("file clean task cleaned file " + file.getName());
                } else {
                    ThreadUtil.sleep(1000);
                }
            }
        }
    }
}
