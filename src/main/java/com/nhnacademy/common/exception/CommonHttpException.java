package com.nhnacademy.common.exception;

import lombok.Getter;

@Getter
public class CommonHttpException extends RuntimeException{

    /**
     * HTTP 상태 코드 (예: 400, 404, 500 등)
     * -- GETTER --
     *  저장된 HTTP 상태 코드를 반환합니다.
     *
     * @return HTTP 상태 코드

     */
    private final int statusCode;

    /**
     * 상태 코드와 메시지를 기반으로 예외 객체를 생성합니다.
     *
     * @param statusCode HTTP 상태 코드
     * @param message    예외 메시지
     */
    public CommonHttpException(final int statusCode, final String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * 상태 코드, 메시지, 원인 예외(cause)를 기반으로 예외 객체를 생성합니다.
     *
     * @param statusCode HTTP 상태 코드
     * @param message    예외 메시지
     * @param cause      원인 예외
     */
    public CommonHttpException(final int statusCode, final String message, final Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
