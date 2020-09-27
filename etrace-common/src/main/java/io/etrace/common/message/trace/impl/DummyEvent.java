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

package io.etrace.common.message.trace.impl;

import io.etrace.common.constant.Constants;
import io.etrace.common.message.trace.Event;

import java.util.HashMap;
import java.util.Map;

public class DummyEvent implements Event {
    private static final String data = "dummy";
    private static final String type = "dummy";
    private static final String name = "dummy";
    private static final String status = Constants.UNSET;

    @Override
    public String getData() {
        return data;
    }

    @Override
    public void setData(String data) {
    }

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public void setId(long id) {

    }

    @Override
    public Map<String, String> getTags() {
        return new HashMap<>();
    }

    @Override
    public void setTags(Map<String, String> tags) {
    }

    @Override
    public void addTag(String key, String value) {

    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public void complete() {

    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {

    }

    @Override
    public void setStatus(Throwable e) {

    }

    @Override
    public long getTimestamp() {
        return 0;
    }
}
