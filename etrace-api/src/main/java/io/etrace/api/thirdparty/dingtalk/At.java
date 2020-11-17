package io.etrace.api.thirdparty.dingtalk;

import java.util.Arrays;
import java.util.List;

public class At {
    Boolean isAtAll;
    List<String> atMobiles;

    public At(Boolean isAtAll, List<String> atMobiles) {
        this.isAtAll = isAtAll;
        this.atMobiles = atMobiles;
    }

    public static At build(String... mobiles) {
        return new At(false, Arrays.asList(mobiles));
    }

    public Boolean getAtAll() {
        return isAtAll;
    }

    public void setAtAll(Boolean atAll) {
        isAtAll = atAll;
    }

    public List<String> getAtMobiles() {
        return atMobiles;
    }

    public void setAtMobiles(List<String> atMobiles) {
        this.atMobiles = atMobiles;
    }
}
