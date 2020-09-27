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

package io.etrace.consumer;

import io.etrace.common.compression.CompressType;
import io.etrace.common.util.TimeHelper;
import io.etrace.consumer.config.ConsumerProperties;
import io.etrace.consumer.service.HDFSService;
import io.etrace.consumer.storage.hadoop.HDFSBucket;
import io.etrace.consumer.storage.hadoop.PathBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HDFSTestIT {

    @Autowired
    private ConsumerProperties consumerProperties;

    @Test
    public void writeAndRead() throws IOException {
        // write
        long currentHour = TimeHelper.getHour(System.currentTimeMillis());
        String filePath = PathBuilder.buildMessagePath("local-dev", currentHour);
        HDFSBucket bucket = new HDFSBucket(CompressType.snappy.code(), consumerProperties.getHdfs().getPath(),
            filePath);
        long position = bucket.writeBlock("test data".getBytes());
        bucket.close();

        //System.out.println(position);
        HDFSService.DataReader dataReader =
            new HDFSService.DataReader(
                HDFSBucket.buildDataFilePathString(consumerProperties.getHdfs().getPath(), filePath));
        byte[] data = dataReader.readMessage(0, (int)position);

        //System.out.println(new String(data));
    }
}
