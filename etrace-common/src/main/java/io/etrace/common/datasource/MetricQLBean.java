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

package io.etrace.common.datasource;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MetricQLBean {
    private String code;
    private String ql;

    private Object functions;
    private String name;

    private List<TagFilter> tagFilters;

    private String baseQl;

    public MetricQLBean(String code, String ql) {
        this.code = code;
        this.ql = ql;
    }

    public MetricQLBean(String code, String ql, Object functions, List<TagFilter> tagFilters) {
        this.code = code;
        this.ql = ql;
        this.functions = functions;
        this.tagFilters = tagFilters;
    }
}
