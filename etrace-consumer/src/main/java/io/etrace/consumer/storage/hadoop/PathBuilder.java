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

package io.etrace.consumer.storage.hadoop;

import java.text.MessageFormat;
import java.util.Date;

public class PathBuilder {

    public static String buildMessagePath(String name, long timestamp) {
        MessageFormat format = new MessageFormat("{0,date,yyyyMMdd}/{0,date,HH}/{1}");
        return format.format(new Object[] {new Date(timestamp), name});
    }
}
