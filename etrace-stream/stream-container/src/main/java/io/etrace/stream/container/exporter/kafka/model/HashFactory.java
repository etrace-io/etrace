package io.etrace.stream.container.exporter.kafka.model;

import com.google.common.base.Strings;

public class HashFactory {

    public static HashStrategy newInstance(String hashType) {
        if (Strings.isNullOrEmpty(hashType)) {
            return null;
        }
        hashType = hashType.toUpperCase();
        if (hashType.equals(HashType.ROUND_ROBIN.name())) {
            return newInstance(HashType.ROUND_ROBIN);
        } else if (hashType.equals(HashType.HASHING.name())) {
            return newInstance(HashType.HASHING);
        }
        return null;
    }

    public static HashStrategy newInstance(HashType shardingType) {
        switch (shardingType) {
            case ROUND_ROBIN:
                return new RoundRobinStrategy();
            case HASHING:
                return new HashingStrategy();
            default:
                break;
        }
        return null;
    }

    public enum HashType {
        ROUND_ROBIN,
        HASHING
    }
}
