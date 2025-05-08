package com.nhnacademy.dashboard.exception;

import com.nhnacademy.common.exception.CommonHttpException;

public class NotFoundException extends CommonHttpException {

    private static final int HTTP_STATUS_CODE = 404;

    public NotFoundException() {
        super(HTTP_STATUS_CODE,"존재하지 않는 resource 입니다.");
    }

    public NotFoundException(String message) {
        super(HTTP_STATUS_CODE, message);
    }
}
