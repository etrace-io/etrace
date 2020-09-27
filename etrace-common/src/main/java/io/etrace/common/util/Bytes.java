/*-
 * ========================LICENSE_START=================================
 * etrace-common
 * %%
 * Copyright (C) 2019 etrace.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.etrace.common.util;

import java.nio.charset.Charset;

public class Bytes {
    private static final String UTF8_ENCODING = "UTF-8";
    public static final Charset UTF8_CHARSET = Charset.forName(UTF8_ENCODING);

    public static byte[] toBytes(int val) {
        byte[] b = new byte[4];
        for (int i = 3; i > 0; i--) {
            b[i] = (byte)val;
            val >>>= 8;
        }
        b[0] = (byte)val;
        return b;
    }
}
