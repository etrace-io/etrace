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

package io.etrace.consumer.config;

import io.etrace.common.pipeline.Resource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "consumer")
@Data
public class ConsumerProperties {
    private int keeper;
    private List<Table> hbase;
    private HDFS hdfs;
    private List<Resource> resources;

    @Data
    public static class Table {
        private String table;
        private List<Shard> distribution;
    }

    @Data
    public static class Shard {
        private String time;
        private short region;
    }

    @Data
    public static class HDFS {
        private String path;
    }

}
