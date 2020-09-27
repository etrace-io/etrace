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

package io.etrace.agent.message;

import io.etrace.common.constant.Constants;

import java.util.UUID;

public class RequestIdAndRpcIdFactory {

    private RandomString randomString = new RandomString(32);

    public RequestIdAndRpcIdFactory() {
    }

    public static String parseClientAppIdFromOriginalRpcId(String rpcId) {
        int index = rpcId.indexOf("|");
        if (index > 0) {
            return rpcId.substring(0, index);
        } else {
            return Constants.UNKNOWN_APP_ID;
        }
    }

    public static String parseRpcIdFromOriginalRpcId(String rpcId) {
        int index = rpcId.indexOf("|");
        if (index > 0) {
            return rpcId.substring(index + 1);
        } else {
            return rpcId;
        }
    }

    public static String buildNextLocalRpcId(String currentRpcId, String nextLocalThreadId) {
        return currentRpcId + "^" + nextLocalThreadId;
    }

    public static String buildNextRemoteRpcId(String rpcId, int currentCall) {
        return rpcId + "." + currentCall;
    }

    public static String buildTruncatedRpcId(String rpcId, int next) {
        return rpcId + "~" + next;
    }

    public static String buildRequestId(String rid, String appId) {
        int index = rid.indexOf("^^");
        if (index < 0) {
            int tsIndex = rid.lastIndexOf("|");
            if (tsIndex > 0) {
                return appId + "^^" + rid;
            }
            return appId + "^^" + rid + "|" + System.currentTimeMillis();
        } else {
            int tsIndex = rid.lastIndexOf("|");
            if (tsIndex < 0) {
                return rid + "|" + System.currentTimeMillis();
            }
        }
        return rid;
    }

    /**
     * old version (before) use random.nextLong() to generate random trace id; now, use UUID (remove '-', and upper
     * case) as random trace id.
     * <p>
     * 2018.1.30: found UUID version (using SecureRandom) cause a little performance cost, so change to custom random
     * string approach, reference to: https://stackoverflow 
     * .com/questions/41107/how-to-generate-a-random-alpha-numeric-string
     *
     * @return {@link String}
     */
    public String getNextId() {
        return randomString.nextString();
    }

    /**
     * use getNextId(), only used for test.
     *
     * @return {@link String}
     */
    @Deprecated
    public String getNextIdFromUUID() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

}
