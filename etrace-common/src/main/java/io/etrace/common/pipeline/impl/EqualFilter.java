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

package io.etrace.common.pipeline.impl;

import io.etrace.common.message.trace.MessageHeader;
import io.etrace.common.pipeline.Filter;
import io.etrace.common.pipeline.Filterable;

import java.util.Map;

public class EqualFilter implements Filter {
    private String key;
    private String name;

    public EqualFilter(String name) {
        this.name = name;
    }

    @Override
    public void init(Map<String, Object> params) {
        key = params.get("key").toString();
    }

    /**
     * ignore case
     *
     * @param filterable
     * @return
     */
    @Override
    public boolean match(Filterable filterable) {
        return key.toLowerCase().equals(filterable.filterKey());
    }

    @Override
    public boolean matchByMessageHeader(MessageHeader messageHeader) {
        return key.toLowerCase().equals(messageHeader.getKey());
    }

    @Override
    public String name() {
        return this.name;
    }
}
