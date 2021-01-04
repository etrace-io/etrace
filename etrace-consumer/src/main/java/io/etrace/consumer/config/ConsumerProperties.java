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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "consumer")
@Data
public class ConsumerProperties {
    private int keeper;
    private List<TableAndRegion> hbase;
    private HDFS hdfs;
    private List<Resource> resources;

    /**
     * 用于配置 hbase特定的 tableName会配置成特殊的 region 数量
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TableAndRegion {
        private String tableName;
        private Integer regionSize;
    }

    @Data
    public static class HDFS {
        private String path;
    }
}
