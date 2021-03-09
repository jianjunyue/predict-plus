package com.predict.plus.algo.features;

public enum FeaturePreprocessType {
    STANDARD(1,"标准化"),
    MIN_MAX(2, "最大最小归一化"),
    QUANTILE(3, "分桶")
    ;

    int code;
    String desc;

    FeaturePreprocessType(int code, String desc) {

        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return this.code;
    }

    public static FeaturePreprocessType fromByCode(int code) {

        for (FeaturePreprocessType value : values()) {
            if (code == value.code) {
                return value;
            }
        }

        return null;
    }
}
