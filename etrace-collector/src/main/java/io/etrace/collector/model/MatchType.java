package io.etrace.collector.model;

public enum MatchType {
    /**
     * prefix match
     */
    PREFIX,
    SUFFIX,
    EQUAL;

    public static boolean match(String key, String matchKey, MatchType type) throws IllegalArgumentException {
        switch (type) {
            case EQUAL:
                return key.equalsIgnoreCase(matchKey);
            case PREFIX:
                return key.startsWith(matchKey);
            case SUFFIX:
                return key.endsWith(matchKey);
            default:
        }
        throw new IllegalArgumentException("Undefined MatchType!" + type);
    }
}
