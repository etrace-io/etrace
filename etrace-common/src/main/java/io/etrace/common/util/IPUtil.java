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

package io.etrace.common.util;

public class IPUtil {

    /**
     * Convert an ip address to an long equivalent to:
     * <p>
     * InetAddress i= InetAddress.getByName("127.0.0.1");
     * <p>
     * int intRepresentation= ByteBuffer.wrap(i.getAddress()).getInt();
     *
     * @param address ip address
     * @return an long number
     */
    public static long ipToLong(String address) {
        try {
            long num = 0;
            long cur = 0;
            for (int i = 0; i < address.length(); i++) {
                char c = address.charAt(i);
                if (c != '.') {
                    int n = c - '0';
                    cur = cur * 10 + n;
                } else {
                    num <<= 8;
                    num += cur;
                    cur = 0;
                }
            }
            num <<= 8;
            num += cur;
            return num;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Convert an long to an ip address
     *
     * @param ipLong an long number
     * @return an ip address
     */
    public static String longToIp(long ipLong) {
        return ((ipLong >> 24) & 0xFF) + "." + ((ipLong >> 16) & 0xFF) + "."
            + ((ipLong >> 8) & 0xFF) + "." + (ipLong & 0xFF);
    }
}
