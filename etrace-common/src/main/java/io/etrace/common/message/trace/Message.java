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

package io.etrace.common.message.trace;

import io.etrace.common.constant.Constants;

import java.util.Map;

public interface Message {
    String SUCCESS = Constants.SUCCESS;

    long getId();

    void setId(long id);

    Map<String, String> getTags();

    void setTags(Map<String, String> tags);

    void addTag(String key, String value);

    String getType();

    void setType(String type);

    String getName();

    void setName(String name);

    void complete();

    boolean isCompleted();

    String getStatus();

    void setStatus(String status);

    void setStatus(Throwable e);

    long getTimestamp();
}
