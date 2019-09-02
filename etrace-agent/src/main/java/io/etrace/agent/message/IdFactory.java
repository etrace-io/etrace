package io.etrace.agent.message;

import java.util.UUID;

public class IdFactory {

    private RandomString randomString = new RandomString(32);

    public IdFactory() {
    }

    /**
     * old version (before) use random.nextLong() to generate random trace id; now, use UUID (remove '-', and upper
     * case) as random trace id.
     * <p>
     * 2018.1.30: found UUID version (using SecureRandom) cause a little performance cost, so change to custom random
     * string approach, reference to: https://stackoverflow
     * .com/questions/41107/how-to-generate-a-random-alpha-numeric-string
     */
    public String getNextId() {
        return randomString.nextString();
    }

    /**
     * use getNextId(), only used for test.
     */
    @Deprecated
    public String getNextIdFromUUID() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
