package com.nhnacademy.dashboard.exception;

public class CommonHttpException extends RuntimeException {

    private final int statusCode;

    public CommonHttpException(final int statusCode, final String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
