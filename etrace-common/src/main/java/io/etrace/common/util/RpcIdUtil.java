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

package io.etrace.common.util;

import com.google.common.collect.Lists;
import io.etrace.common.RequestIdInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RpcIdUtil {

    final static Pattern PAT = Pattern.compile("([.^]+)");

    public static Comparator<String> getComparatorAsString() {
        return (o1, o2) -> {
            if (o1.equals(o2)) {
                return 0;
            }
            String[] parts1 = parseToParts2(o1);
            String[] parts2 = parseToParts2(o2);

            // first, compare same part
            int sameLength = Math.min(parts1.length, parts2.length);
            for (int i = 0; i < sameLength; i++) {
                if (!parts1[i].equals(parts2[i])) {
                    try {
                        return Integer.parseInt(parts1[i]) - Integer.parseInt(parts2[i]);
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }

            // then, compare the remaining length: (parts1.length - sameLength) - (parts2.length - sameLength);
            return parts1.length - parts2.length;
        };
    }

    public static Comparator<RequestIdInfo> getComparator() {
        return (o1, o2) -> {
            if (o1.getRpcId().equals(o2.getRpcId())) {
                return 0;
            }
            String[] parts1 = parseToParts2(o1.getRpcId());
            String[] parts2 = parseToParts2(o2.getRpcId());

            // first, compare same part
            int sameLength = Math.min(parts1.length, parts2.length);
            for (int i = 0; i < sameLength; i++) {
                if (!parts1[i].equals(parts2[i])) {
                    try {
                        return Long.compare(Long.parseLong(parts1[i]), Long.parseLong(parts2[i]));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
            }

            // then, compare the remaining length: (parts1.length - sameLength) - (parts2.length - sameLength);
            return parts1.length - parts2.length;
        };
    }

    public static String[] parseToParts2(String rpcId) {
        String[] parts = rpcId.split("[.^]+");

        if (parts.length == 1) {
            return parts;
        } else {
            List<String> result = Lists.newArrayList();
            List<String> regexParts = matchAll(PAT, rpcId);

            int index = 0;
            boolean meetRemoveCall = false;
            Stack<String> stack = new Stack<>();

            while (index < parts.length) {
                if (index < regexParts.size() && "^".equals(regexParts.get(index))) {
                    stack.push(parts[index]);
                    meetRemoveCall = true;
                } else {
                    if (meetRemoveCall) {
                        stack.push(parts[index]);

                        result.addAll(popAndClear(stack));
                        meetRemoveCall = false;
                    } else {
                        result.add(parts[index]);
                    }
                }
                index++;
            }
            return result.toArray(new String[0]);
        }
    }

    public static List<String> matchAll(Pattern pat, String input) {
        List<String> result = Lists.newArrayList();
        Matcher matcher = pat.matcher(input);

        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    private static <T> List<T> popAndClear(Stack<T> stack) {
        List<T> result = Lists.newArrayListWithCapacity(stack.size());
        while (!stack.empty()) {
            result.add(stack.pop());
        }
        return result;
    }

    // not used
    @Deprecated
    public static String[] parseToParts(String rpcId) {
        if (rpcId.contains("^")) {
            int index = 0;
            List<String> list = Lists.newArrayList();
            Stack<String> angleStack = new Stack<>();

            while (index < rpcId.length()) {
                int nextPeriod = rpcId.indexOf(".", index);
                int nextAngle = rpcId.indexOf("^", index);

                int nextPartStart;

                if (nextPeriod < 0 && nextAngle > 0) {
                    angleStack.add(rpcId.substring(index, nextAngle));
                    nextPartStart = nextAngle;
                } else if (nextPeriod > 0 && nextAngle < 0) {
                    list.add(rpcId.substring(index, nextPeriod));
                    nextPartStart = nextPeriod;
                } else if (nextPeriod < nextAngle) {
                    if (!angleStack.empty()) {
                        list.addAll(new ArrayList<>(angleStack));
                        angleStack.clear();
                    }
                    list.add(rpcId.substring(index, nextPeriod));
                    nextPartStart = nextPeriod;
                } else {
                    // nextPeriod > nextAngle
                    // handle "^"
                    angleStack.push(rpcId.substring(index, nextAngle));
                    nextPartStart = nextAngle;
                }

                // handle "^^" case
                while (nextPartStart + 1 < rpcId.length()
                    && rpcId.charAt(nextPartStart + 1) != '.'
                    && rpcId.charAt(nextPartStart + 1) != '^') {
                    nextPartStart++;
                }

                int nextNextPeriod = rpcId.indexOf(".", nextPartStart + 1);
                int nextNextAngle = rpcId.indexOf("^", nextPartStart + 1);

                if (nextNextAngle < 0 && nextNextPeriod < 0) {
                    list.add(rpcId.substring(index));
                    index = rpcId.length();
                } else if (nextNextPeriod < 0) {
                    index = nextNextAngle;
                } else if (nextNextAngle < 0) {
                    index = nextNextPeriod;
                } else {
                    index = Math.min(nextNextAngle, nextNextPeriod);
                }
            }

            if (!angleStack.empty()) {
                list.addAll(new ArrayList<>(angleStack));
                angleStack.clear();
            }
            return list.toArray(new String[0]);
        } else {
            return rpcId.split("[.^]");
        }
    }
}


