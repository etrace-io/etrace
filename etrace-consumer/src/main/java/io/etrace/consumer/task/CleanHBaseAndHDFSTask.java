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

package io.etrace.consumer.task;

import com.google.common.base.Joiner;
import io.etrace.agent.Trace;
import io.etrace.common.constant.Constants;
import io.etrace.common.message.trace.Transaction;
import io.etrace.common.util.ThreadUtil;
import io.etrace.common.util.TimeHelper;
import io.etrace.consumer.config.ConsumerProperties;
import io.etrace.consumer.storage.hadoop.FileSystemManager;
import io.etrace.consumer.storage.hbase.HBaseClientFactory;
import io.etrace.consumer.storage.hbase.impl.MetricImpl;
import io.etrace.consumer.storage.hbase.impl.StackImpl;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

@Component
public class CleanHBaseAndHDFSTask {
    private final static Logger LOGGER = LoggerFactory.getLogger(CleanHBaseAndHDFSTask.class);
    @Autowired
    public ConsumerProperties consumerProperties;
    @Autowired
    public MetricImpl metricImpl;
    @Autowired
    public StackImpl stackImpl;
    @Autowired
    private HBaseClientFactory hBaseClientFactory;

    private static void deleteByPath(FileSystem fs, Path path) throws IOException {
        LOGGER.info("{} will be deleted", path);
        fs.delete(path, true);
    }

    @Scheduled(initialDelay = 10 * 60 * 1000, fixedRate = 24 * 60 * 60 * 1000)
    public void deleteHBaseTable() {
        int keeper = consumerProperties.getKeeper();
        long current = System.currentTimeMillis();
        Set<Integer> keepSet = newHashSet();

        Transaction transaction = Trace.newTransaction("DELETE_HBASE", "keeper:" + keeper);
        try {
            //keeper last keeper day table
            for (int i = 0; i < keeper + 1; i++) {
                keepSet.add(TimeHelper.getDay(current - i * TimeHelper.ONE_DAY));
            }
            //keeper future x day table
            for (int i = 1; i < 3; i++) {
                keepSet.add(TimeHelper.getDay(current + i * TimeHelper.ONE_DAY));
            }
            LOGGER.info("keeper hbase table is [{}].", Joiner.on(",").join(keepSet));

            if (keepSet.size() < keeper + 2) {
                LOGGER.warn("keeper day is wrong, data is [{}].", Joiner.on(",").join(keepSet));
                return;
            }
            for (int i = 1; i <= 31; i++) {
                if (!keepSet.contains(i)) {
                    List<String> deleteTables = newArrayList();
                    String tableName = stackImpl.getName() + "_" + i;
                    tryDeleteTable(tableName);
                    deleteTables.add(tableName);
                    tableName = metricImpl.getName() + "_" + i;
                    tryDeleteTable(tableName);
                    deleteTables.add(tableName);
                    transaction.addTag("tables_" + i, deleteTables.toString());
                }
            }
            transaction.setStatus(Constants.SUCCESS);
        } catch (Throwable e) {
            transaction.setStatus(e);
        } finally {
            transaction.complete();
        }
    }

    public void tryDeleteTable(String tableName) {
        int tryCount = 0;
        //try delete
        while (++tryCount < 4) {
            try {
                hBaseClientFactory.deleteTable(tableName);
                LOGGER.info("Delete hbase table {} success.", tableName);
                break;
            } catch (Exception e) {
                LOGGER.error("Delete history table error:", e);
                ThreadUtil.sleepForSecond(3);
            }
        }
    }

    @Scheduled(initialDelay = 10 * 60 * 1000, fixedRate = 24 * 60 * 60 * 1000)
    public void deleteHDFS() {
        int doKeeper = consumerProperties.getKeeper();
        String remotePath = consumerProperties.getHdfs().getPath();

        List<String> deleteFiles = newArrayList();
        Transaction transaction = Trace.newTransaction("DELETE_HDFS", "keeper:" + doKeeper);
        try {
            Path stackPath = new Path(remotePath + File.separator + "stack");
            List<Path> deletePaths = newArrayList();
            FileSystem fs = FileSystemManager.getFileSystem();
            FileStatus[] files = fs.listStatus(stackPath);
            Date keeperData = new Date(System.currentTimeMillis() - doKeeper * TimeHelper.ONE_DAY);
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            for (FileStatus file : files) {
                String[] path = file.getPath().getName().split("/");
                Date date = format.parse(path[path.length - 1]);
                if (date.before(keeperData)) {
                    deleteFiles.add(file.getPath().toString());
                    deletePaths.add(file.getPath());
                }
            }
            transaction.addTag("files", deleteFiles.toString());
            for (Path path : deletePaths) {
                deleteByPath(fs, path);
            }
            transaction.setStatus(Constants.SUCCESS);
        } catch (IllegalArgumentException | IOException | ParseException e) {
            LOGGER.error("delete hdfs error:", e);
            transaction.setStatus(e);
        } finally {
            transaction.complete();
        }
    }
}
