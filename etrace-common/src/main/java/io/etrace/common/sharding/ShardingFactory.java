package io.etrace.common.sharding;

import com.google.common.base.Strings;
import io.etrace.common.sharding.impl.HashingStrategy;
import io.etrace.common.sharding.impl.RoundRobinStrategy;

public class ShardingFactory {

    public static ShardingStrategy newInstance(String shardingType) {
        if (Strings.isNullOrEmpty(shardingType)) {
            return null;
        }
        shardingType = shardingType.toUpperCase();
        if (shardingType.equals(ShardingType.ROUND_ROBIN.name())) {
            return newInstance(ShardingType.ROUND_ROBIN);
        } else if (shardingType.equals(ShardingType.HASH.name())) {
            return newInstance(ShardingType.HASH);
        }
        return null;
    }

    public static ShardingStrategy newInstance(ShardingType shardingType) {
        switch (shardingType) {
            case ROUND_ROBIN:
                return new RoundRobinStrategy();
            case HASH:
                return new HashingStrategy();
            default:
                break;
        }
        return null;
    }

    public enum ShardingType {
        ROUND_ROBIN,
        HASH
    }
}
