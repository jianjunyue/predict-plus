package com.predict.plus.algo.exception;


public class FeatureException extends RuntimeException {
    public FeatureException(String message) {
        super(message);
    }
    public FeatureException(Exception e, String message) {
        super(message, e);
    }
}
