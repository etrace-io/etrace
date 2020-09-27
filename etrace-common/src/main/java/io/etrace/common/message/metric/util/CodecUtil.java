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


package io.etrace.common.message.metric.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CodecUtil {

    public static byte[] readLengthPrefixData(DataInputStream in) throws IOException {
        int dataLen = in.readInt();
        if (dataLen == 0) {
            return null;
        }
        byte[] data = new byte[dataLen];
        in.read(data, 0, dataLen);
        return data;
    }

    public static void writeLengthPrefixData(DataOutputStream out, byte[] data) throws IOException {
        int dataLen;
        if (data == null) {
            out.writeInt(0);
        } else {
            dataLen = data.length;
            out.writeInt(dataLen);
            out.write(data);
        }
    }

}
