package com.nhnacademy.dashboard.exception;

import com.nhnacademy.common.exception.CommonHttpException;

public class BadRequestException extends CommonHttpException {

    private static final int STATUS_CODE = 400;

    public BadRequestException(String message) {
        super(STATUS_CODE, message);
    }
}
