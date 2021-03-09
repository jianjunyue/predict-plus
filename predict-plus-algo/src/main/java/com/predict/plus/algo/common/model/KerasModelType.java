package com.predict.plus.algo.common.model;

public enum KerasModelType {
    Keras_Model("Model", "Keras Model"),
    Keras_Sequential("Sequential", "Keras Sequential");
    String code;
    String desc;

    KerasModelType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static KerasModelType fromCode(String code) {
        KerasModelType[] values = values();
        for (KerasModelType value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
