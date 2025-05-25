package com.nhnacademy.common.advice;

import com.nhnacademy.common.exception.CommonHttpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class CommonAdvice {

    /**
     * 어노테이션 Valid 검증 실패 시 발생하는 MethodArgumentNotValidException 예외를 처리합니다.
     *
     * @param ex 유효성 검사 실패 정보를 담고 있는 MethodArgumentNotValidException 객체
     * @return 필드명과 오류 메시지를 담은 Map을 포함한 ResponseEntity로, HTTP 400 (Bad Request) 상태를 반환합니다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * 요청 본문(@RequestBody)이 없거나 JSON 형식이 잘못되었을 때 발생하는 예외를 처리합니다.
     *
     * @param ex {@link HttpMessageNotReadableException} 예외 객체
     * @return 400 Bad Request와 오류 메시지를 포함한 응답
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleMissingRequestBody(HttpMessageNotReadableException ex) {
        log.error("handleMissingRequestBody Error: {}", ex.getMessage(), ex);

        String message = ex.getMessage() != null ? ex.getMessage() : "요청 본문이 없습니다.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Request body 없거나 잘못된 형식입니다: "+message);
    }

    /**
     * 요청 헤더(@RequestHeader)가 누락되었을 때 발생하는 예외를 처리합니다.
     *
     * @param ex {@link MissingRequestHeaderException} 예외 객체
     * @return 400 Bad Request와 오류 메시지를 포함한 응답
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        log.error("handleMissingRequestHeader Error: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Required request header 없습니다: "+ex.getMessage());
    }

    /**
     * {@link CommonHttpException} 예외 처리 메서드입니다.
     * <p>
     * 커스텀 예외에 포함된 상태 코드를 기반으로 HTTP 응답 상태를 지정합니다.
     * </p>
     *
     * @param e CommonHttpException 예외 객체
     * @return 정의된 상태 코드와 함께 메시지를 포함한 응답
     */
    @ExceptionHandler(CommonHttpException.class)
    public ResponseEntity<String> commonExceptionHandler(CommonHttpException e) {
        log.warn("CommonHttpException 발생: {}", e.getMessage());

        return ResponseEntity
                .status(e.getStatusCode())
                .body("CommonException: " + e.getMessage());
    }

    /**ad
     * 그 외 모든 예외(Throwable)를 처리하는 메서드입니다.
     * <p>
     * 예상하지 못한 예외가 발생했을 때 서버 내부 에러(500)로 응답합니다.
     * 운영 환경에서는 자세한 정보를 노출하지 않도록 주의합니다.
     * </p>
     *
     * @param e 처리되지 않은 모든 예외
     * @return 500 Internal Server Error 응답
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> exceptionHandler(Throwable e) {
        log.error("Internal Server Error: {}", e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버에서 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }
}
