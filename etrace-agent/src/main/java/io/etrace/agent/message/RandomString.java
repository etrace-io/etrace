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
     */
    public RandomString(int length) {
        this(length, alphanum);
    }

    /**
     * Generate a random string.
     */
    public String nextString() {
        char[] xx = new char[buf.length];
        for (int idx = 0; idx < xx.length; ++idx) {
            xx[idx] = symbols[ThreadLocalRandom.current().nextInt(symbols.length)];
        }
        return new String(xx);
    }
}
