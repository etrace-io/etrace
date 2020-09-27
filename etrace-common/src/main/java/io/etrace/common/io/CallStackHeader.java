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

package io.etrace.common.io;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class CallStackHeader {
    private long timestamp;
    private byte compressType;
    private List<Integer> offsets;
    private Map<Integer, String> instances = Maps.newHashMap();

    public CallStackHeader(byte compressType, List<Integer> offsets, long timestamp, Map<Integer, String> instances) {
        this.compressType = compressType;
        this.offsets = offsets;
        this.timestamp = timestamp;
        this.instances = instances;
    }
}
