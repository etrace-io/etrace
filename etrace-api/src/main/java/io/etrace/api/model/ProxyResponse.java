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

package io.etrace.api.model;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ProxyResponse {
    public HttpStatus responseStatus;
    private Map<String, String> msg = new HashMap<>();

    private List<Object> result = Lists.newArrayList();

    public void addResult(HttpStatus status, Object body) {
        if (null == this.responseStatus || this.responseStatus != HttpStatus.OK) {
            this.responseStatus = status;
        }
        if (body instanceof List) {
            result.addAll((List)body);
        } else {
            result.add(body);
        }
    }

}
