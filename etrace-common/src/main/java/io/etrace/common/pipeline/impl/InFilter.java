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

import com.google.common.collect.Sets;
import io.etrace.common.pipeline.AbstractFilter;

import java.util.Map;
import java.util.Set;

// todo: 重复了
public class InFilter extends AbstractFilter {
    private Set<String> contains;
    private String name;

    public InFilter(String name) {
        this.name = name;
    }

    // todo 这里的 match 逻辑有问题
    @Override
    public boolean isMatch(Object obj) {
        return contains.contains(obj.toString());
    }

    @Override
    public void init(Map<String, Object> params) {
        try {
            contains = Sets.newHashSet(params.get("key").toString().split(","));
        } catch (Exception ex) {
            String msg = "init InFilter error, events should be provided in params as string list, actual params: ["
                + params + "]";
            throw new RuntimeException(msg);
        }
    }

    @Override
    public String name() {
        return this.name;
    }
}
