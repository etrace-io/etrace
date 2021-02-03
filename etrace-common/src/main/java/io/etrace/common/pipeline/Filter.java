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

package io.etrace.common.pipeline;

import io.etrace.common.message.trace.MessageHeader;

import java.util.Map;

public interface Filter {

    void init(Map<String, Object> params);

    boolean match(Filterable filterable);

    boolean matchByMessageHeader(MessageHeader messageHeader);

    String name();

    //// todo:  不得已而为之，Component.dispatch() 不好处理
    //default boolean matchByString(String s) {
    //    return match(new FilterableString(s));
    //}
    //
    //String name();
    //
    //class FilterableString implements Filterable {
    //    String key;
    //
    //    public FilterableString(String key) {
    //        this.key = key;
    //    }
    //
    //    @Override
    //    public String filterKey() {
    //        return key;
    //    }
    //}
}
