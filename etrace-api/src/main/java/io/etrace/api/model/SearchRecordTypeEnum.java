package io.etrace.api.model;

import lombok.Getter;

@Getter
public enum SearchRecordTypeEnum {
    /**
     * 系统类型，
     */
    APP(0, "系统"),
    /**
     * 文档类型
     */
    LINK(1, "连接");

    private Integer code;
    private String desc;

    SearchRecordTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SearchRecordTypeEnum ofCode(Integer code) {
        if (null == code) {
            return null;
        }

        switch (code) {
            case 0:
                return APP;
            case 1:
                return LINK;
            default:
                return null;
        }
    }

    public static SearchRecordTypeEnum ofName(String name) {
        if (null == name) {
            return null;
        }
        switch (name) {
            case "APP":
                return APP;
            case "LINK":
                return LINK;
            default:
                return null;
        }
    }

}
