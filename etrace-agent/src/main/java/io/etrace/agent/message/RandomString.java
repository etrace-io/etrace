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

import java.util.concurrent.ThreadLocalRandom;

/**
 * copied from:  https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string NOTICE: the
 * code from stackoverflow is not ThreadSafe !!!
 */
public class RandomString {

    private static final String upper = "ABCDEF";
    private static final String digits = "0123456789";
    private static final String alphanum = upper + digits;
    private final char[] symbols;
    private final char[] buf;

    public RandomString(int length, String symbols) {
        if (length < 1) { throw new IllegalArgumentException(); }
        if (symbols.length() < 2) { throw new IllegalArgumentException(); }
        this.symbols = symbols.toCharArray();
        this.buf = new char[length];
    }

    /**
     * Create an alphanumeric string generator.
     *
     * @param length length
     */
    public RandomString(int length) {
        this(length, alphanum);
    }

    /**
     * Generate a random string.
     *
     * @return {@link String}
     */
    public String nextString() {
        char[] xx = new char[buf.length];
        for (int idx = 0; idx < xx.length; ++idx) {
            xx[idx] = symbols[ThreadLocalRandom.current().nextInt(symbols.length)];
        }
        return new String(xx);
    }
}
