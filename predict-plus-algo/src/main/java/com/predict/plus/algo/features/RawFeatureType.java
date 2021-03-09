package com.predict.plus.algo.features;


public enum RawFeatureType {
    Number(1,"数字类型原始特征"),
    Text(2, "文本类型原始特征")
    ;

    int code;
    String desc;

    RawFeatureType(int code, String desc) {

        this.code = code;
        this.desc = desc;
    }

    public static RawFeatureType fromByCode(int code) {

        for (RawFeatureType value : values()) {
            if (code == value.code) {
                return value;
            }
        }

        return null;
    }

}
