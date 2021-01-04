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

import com.google.common.collect.Maps;
import io.etrace.agent.Trace;
import io.etrace.common.constant.Constants;
import io.etrace.common.message.trace.Transaction;
import io.etrace.common.util.ThreadUtil;
import io.etrace.common.util.TimeHelper;
import io.etrace.consumer.config.ConsumerProperties;
import io.etrace.consumer.storage.hadoop.FileSystemManager;
import io.etrace.consumer.storage.hbase.IHBaseClientFactory;
import io.etrace.consumer.storage.hbase.IHBaseTableNameFactory;
import io.etrace.consumer.storage.hbase.impl.MetricImpl;
import io.etrace.consumer.storage.hbase.impl.StackImpl;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

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
    private IHBaseClientFactory iHBaseClientFactory;

    @Autowired
    private IHBaseTableNameFactory ihBaseTableNameFactory;

    @Autowired
    ApplicationContext context;

    Map<String, Integer> hbaseTableToClean = Maps.newHashMap();

    private static void deleteByPath(FileSystem fs, Path path) throws IOException {
        LOGGER.info("{} will be deleted", path);
        fs.delete(path, true);
    }

    public void registerTableToDelete (String logicHBaseTableName, int daysToClean) {
        hbaseTableToClean.put(logicHBaseTableName, daysToClean);
    }

    @Scheduled(initialDelay = 10 * 60 * 1000 , fixedRate = 24 * 60 * 60 * 1000)
    public void cleanHBaseTables() {
        hbaseTableToClean.forEach(this::deleteHBaseTable);
    }

    private void deleteHBaseTable(String logicHBaseTableName, int daysToClean) {
        long current = System.currentTimeMillis();
        Set<Integer> retainedTables = newHashSet();

        try {
            //retain last x day
            for (int i = 0; i < daysToClean + 1; i++) {
                retainedTables.add(TimeHelper.getDay(current - i * TimeHelper.ONE_DAY));
            }

            //retain future 2 day
            for (int i = 1; i < 3; i++) {
                retainedTables.add(TimeHelper.getDay(current + i * TimeHelper.ONE_DAY));
            }
            LOGGER.info("going to retain hbase table {}, other days will be deleted.", retainedTables);

            // Tables to delete
            IntStream.rangeClosed(1,31)
                .filter(day -> !retainedTables.contains(day))
                .forEach(day -> {
                String tableName = ihBaseTableNameFactory.getPhysicalTableNameByTableNamePrefix(logicHBaseTableName, day);
                tryDeleteTable(tableName);
            });
        } catch (Throwable e) {
            LOGGER.error("==deleteHBaseTable==", e);
        }
    }

    public void tryDeleteTable(String tableName) {
        int tryCount = 0;
        while (++tryCount < 4) {
            try {
                iHBaseClientFactory.deleteTable(tableName);
                LOGGER.info("Delete hbase table {} success.", tableName);
                break;
            } catch (Exception e) {
                LOGGER.error("Delete history table error:", e);
                ThreadUtil.sleepForSecond(3);
            }
        }
    }

    @Scheduled(initialDelay = 10 * 60 * 1000, fixedRate = 24 * 60 * 60 * 1000)
    public void deleteHDFSFiles() {
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
