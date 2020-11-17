package io.etrace.api.consts;

import lombok.Getter;

@Getter
public enum SearchListTypeEnum {
    /**
     * 推荐，即置顶
     */
    RECOMMEND(0, "推荐"),
    /**
     * 热搜,前一天累计查看最多的
     */
    HOTEST(1, "热搜"),
    /**
     * 最新添加的
     */
    NEWEST(2, "最新");

    private final int code;
    private final String desc;

    SearchListTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SearchListTypeEnum ofCode(int code) {
        switch (code) {
            case 0:
                return RECOMMEND;
            case 1:
                return HOTEST;
            case 2:
                return NEWEST;
            default:
                return null;
        }
    }
}
