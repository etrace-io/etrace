package io.etrace.common.modal;

import com.google.common.base.Strings;
import io.etrace.common.exception.TooManyRedisException;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class RedisStats extends AbstractMessage {
    private String url;
    private Map<String, RedisCommandStats> commands = new HashMap<>();

    public RedisStats(String url) {
        this.url = url;
        timestamp = System.currentTimeMillis();
    }

    public int merge(String url, String command, long duration, boolean succeed, RedisResponse[] responses,
                     boolean stopNew) {
        int raiseCommand = 0;
        if (this.url == null || url == null || !this.url.equals(url) || Strings.isNullOrEmpty(url)) {
            return raiseCommand;
        }
        RedisCommandStats redisCommandStats = this.commands.get(command);
        if (redisCommandStats == null) {
            if (stopNew) {
                throw new TooManyRedisException("Too many redis, stop new commands");
            }
            raiseCommand++;
            redisCommandStats = new RedisCommandStats(command);
            this.commands.put(command, redisCommandStats);
        }
        redisCommandStats.merge(duration, succeed, responses);
        return raiseCommand;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, RedisCommandStats> getCommands() {
        return commands;
    }

    public RedisCommandStats newRedisCommandStats() {
        return new RedisCommandStats(null);
    }

    public void add(RedisCommandStats command) {
        if (Strings.isNullOrEmpty(command.getCommand())) {
            return;
        }
        this.commands.put(command.getCommand(), command);
    }

    public long getAllDuration() {
        long duration = 0;
        for (RedisCommandStats redisCommandStats : commands.values()) {
            duration += redisCommandStats.getDurationFailSum();
            duration += redisCommandStats.getDurationSucceedSum();
        }
        return duration;
    }

    public long getAllCount() {
        long count = 0;
        for (RedisCommandStats redisCommandStats : commands.values()) {
            count += redisCommandStats.getSucceedCount();
            count += redisCommandStats.getFailCount();
        }
        return count;
    }

    @Override
    public void complete() {
    }

    public class RedisCommandStats {
        private String command;
        private long succeedCount = 0;
        private long failCount = 0;
        private long durationSucceedSum = 0;
        private long durationFailSum = 0;
        private long maxDuration = -1;
        private long minDuration = -1;
        private long responseCount = -1;
        private long hitCount = -1;
        private long responseSizeSum = -1;
        private long maxResponseSize = -1;
        private long minResponseSize = -1;

        public RedisCommandStats(String command) {
            this.command = command;
        }

        public void merge(long duration, boolean succeed, RedisResponse[] responses) {
            if (succeed) {
                succeedCount++;
                durationSucceedSum += duration;
            } else {
                failCount++;
                durationFailSum += duration;
            }
            if (-1 == maxDuration || maxDuration < duration) {
                maxDuration = duration;
            }
            if (-1 == minDuration || minDuration > duration) {
                minDuration = duration;
            }
            if (responses == null || responses.length <= 0) {
                return;
            }

            for (RedisResponse redisResponse : responses) {
                if (redisResponse == null) {
                    continue;
                }
                initCount();
                responseCount++;
                if (redisResponse.isHit()) {
                    hitCount++;
                }
                int responseSize = redisResponse.getResponseSize();
                this.responseSizeSum += responseSize;
                if (-1 == maxResponseSize || maxResponseSize < responseSize) {
                    maxResponseSize = responseSize;
                }
                if (-1 == minResponseSize || minResponseSize > responseSize) {
                    minResponseSize = responseSize;
                }
            }
        }

        private void initCount() {
            if (-1 == responseCount) {
                responseCount = 0;
                hitCount = 0;
                responseSizeSum = 0;
            }
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public long getSucceedCount() {
            return succeedCount;
        }

        public void setSucceedCount(long succeedCount) {
            this.succeedCount = succeedCount;
        }

        public long getFailCount() {
            return failCount;
        }

        public void setFailCount(long failCount) {
            this.failCount = failCount;
        }

        public long getDurationSucceedSum() {
            return durationSucceedSum;
        }

        public void setDurationSucceedSum(long durationSucceedSum) {
            this.durationSucceedSum = durationSucceedSum;
        }

        public long getDurationFailSum() {
            return durationFailSum;
        }

        public void setDurationFailSum(long durationFailSum) {
            this.durationFailSum = durationFailSum;
        }

        public long getMaxDuration() {
            return maxDuration;
        }

        public void setMaxDuration(long maxDuration) {
            this.maxDuration = maxDuration;
        }

        public long getMinDuration() {
            return minDuration;
        }

        public void setMinDuration(long minDuration) {
            this.minDuration = minDuration;
        }

        public long getResponseCount() {
            return responseCount;
        }

        public void setResponseCount(long responseCount) {
            this.responseCount = responseCount;
        }

        public long getHitCount() {
            return hitCount;
        }

        public void setHitCount(long hitCount) {
            this.hitCount = hitCount;
        }

        public long getResponseSizeSum() {
            return responseSizeSum;
        }

        public void setResponseSizeSum(long responseSizeSum) {
            this.responseSizeSum = responseSizeSum;
        }

        public long getMaxResponseSize() {
            return maxResponseSize;
        }

        public void setMaxResponseSize(long maxResponseSize) {
            this.maxResponseSize = maxResponseSize;
        }

        public long getMinResponseSize() {
            return minResponseSize;
        }

        public void setMinResponseSize(long minResponseSize) {
            this.minResponseSize = minResponseSize;
        }
    }

}
