package io.etrace.common.modal;

public class RedisResponse {
    private boolean hit;
    private int responseSize;

    public RedisResponse(boolean hit, int responseSize) {
        this.hit = hit;
        this.responseSize = responseSize;
    }

    public boolean isHit() {
        return hit;
    }

    public int getResponseSize() {
        return responseSize;
    }
}
