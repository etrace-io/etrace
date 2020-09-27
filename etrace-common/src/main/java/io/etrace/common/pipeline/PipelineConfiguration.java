/*
 * Copyright 2020 etrace.io
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

package io.etrace.common.pipeline;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PipelineConfiguration {

    private boolean enable = true;

    private String name;
    private List<Channel> receivers;
    private List<Channel> exporters;
    private List<TaskProp> processors;
    private List<TaskProp> filters;
    private List<RouteProp> pipelines;

    @Data
    public static class Channel extends TaskProp {
        private Channel.Type type;

        public enum Type {
            THRIFT,
            TCP,
            GRPC,
            KAFKA,
            LINDB,
            HDFS,
            HBASE,
            ES,
            PROMETHEUS
        }
    }

    @Data
    public static class TaskProp {
        private String name;
        private String clazz;
        private Map<String, Object> props;
    }

    @Data
    public static class RouteProp {
        private String name;
        private List<DownStreamProp> downstreams;
    }

    @Data
    public static class DownStreamProp {
        private String filter;
        private List<String> components;
    }

}
