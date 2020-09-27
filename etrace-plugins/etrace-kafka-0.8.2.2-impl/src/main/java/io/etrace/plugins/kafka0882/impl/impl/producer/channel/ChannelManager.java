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

package io.etrace.plugins.kafka0882.impl.impl.producer.channel;

import io.etrace.plugins.kafka0882.impl.impl.IntObjectHashMap;

import java.util.ArrayList;
import java.util.List;

public class ChannelManager {
    private int memorySize;
    private IntObjectHashMap<Channel> channels = new IntObjectHashMap<>(32);

    public ChannelManager(int memorySize) {
        this.memorySize = memorySize;
    }

    public synchronized Channel putMemoryChannel(int id) {
        Channel channel = channels.get(id);
        if (channel == null) {
            // init The Channel
            channel = new MemoryChannel(this.memorySize);
            channels.put(id, channel);
        }
        return channel;
    }

    public List<Integer> getOverflowIds() {
        List<Integer> overflowIds = new ArrayList<>();
        for (int i = 0; i < channels.size(); i++) {
            Channel channel = channels.get(i);
            if (null != channel) {
                if (!channel.isFull()) {
                    continue;
                }
                overflowIds.add(i);
            }
        }
        return overflowIds;
    }
}
