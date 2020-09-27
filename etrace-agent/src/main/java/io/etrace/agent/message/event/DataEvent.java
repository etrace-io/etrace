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

package io.etrace.agent.message.event;

import com.lmax.disruptor.EventFactory;

public class DataEvent {
    private byte[] buffer;
    private int count;

    public byte[] getBuffer() {
        return buffer;
    }

    public int getCount() {
        return count;
    }

    public void reset(byte[] data, int count) {
        this.buffer = data;
        this.count = count;
    }

    public void clear() {
        this.buffer = null;
        this.count = 0;
    }

    public static class DataEventFactory implements EventFactory<DataEvent> {

        @Override
        public DataEvent newInstance() {
            return new DataEvent();
        }
    }
}
