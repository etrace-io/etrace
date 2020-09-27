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

package io.etrace.common.message.trace;

public enum MessageType {
    EVENT((byte)1),
    TRANSACTION((byte)2),
    HEARTBEAT((byte)3),
    UNKNOWN((byte)100);

    private byte code;

    MessageType(byte code) {
        this.code = code;
    }

    public static MessageType findByCode(byte code) {
        switch (code) {
            case 1:
                return EVENT;
            case 2:
                return TRANSACTION;
            case 3:
                return HEARTBEAT;
            default:
                return UNKNOWN;
        }
    }

    public byte code() {
        return code;
    }
}
