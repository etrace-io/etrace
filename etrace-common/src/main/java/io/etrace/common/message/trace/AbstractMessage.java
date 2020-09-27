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

package io.etrace.common.message.trace;

import io.etrace.common.constant.Constants;
import io.etrace.common.util.SimpleArrayMap;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public abstract class AbstractMessage implements Message {
    public static final int TYPE_TRUNCATE_SIZE = 256;
    public static final int NAME_TRUNCATE_SIZE = 512;
    public static final int STATUS_TRUNCATE_SIZE = 64;
    public static final int TAG_KEY_TRUNCATE_SIZE = 64;
    public static final int TAG_VALUE_TRUNCATE_SIZE = 128;
    protected String type;
    protected String name;
    protected String status = Constants.UNSET;
    protected long timestamp;
    protected boolean completed;
    protected Map<String, String> tags;
    protected TraceManager manager;
    protected long id;

    public AbstractMessage(String type, String name, TraceManager manager) {
        this.type = type;
        this.name = name;
        this.manager = manager;
        timestamp = System.currentTimeMillis();
    }

    protected void addTagsForJsonDecode(Map<String, String> tags) {
        if (this.tags == null) {
            this.tags = new HashMap<>();
        }
        this.tags.putAll(tags);
    }

    protected void addTags(Map<String, String> tags) {
        if (completed) {
            return;
        }
        if (this.tags == null) {
            this.tags = new HashMap<>();
        }
        this.tags.putAll(tags);
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public void setTags(Map<String, String> tags) {
        if (tags != null && tags.size() > 0) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                addTag(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void addTag(String key, String value) {
        if (completed) {
            return;
        }
        try {
            if (manager != null) {
                int tagCount = this.manager.getConfigManager().getTagCount();
                if (tags == null) {
                    tags = new SimpleArrayMap<>(tagCount);
                }
                tags.put(key, value);
            } else {
                if (tags == null) {
                    tags = new SimpleArrayMap<>(8);
                }
                tags.put(key, value);
            }
        } catch (Exception ignore) {
        }
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void setStatus(Throwable e) {

    }
}
