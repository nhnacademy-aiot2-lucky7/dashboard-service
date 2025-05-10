package com.nhnacademy.common.decoder;

import com.nhnacademy.common.exception.CommonHttpException;
import feign.Request;
import feign.Response;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class FeignErrorDecoderTest {

    private FeignErrorDecoder decoder = new FeignErrorDecoder();

    @Test
    @DisplayName("feignError 문구 출력")
    void decode() {

        String responseBody = "에러 메시지입니다.";
        Response response = Response.builder()
                .status(400)
                .reason("Bad Request")
                .request(Request.create(Request.HttpMethod.GET, "/api/test", Collections.emptyMap(), null, null, null))
                .body(responseBody, StandardCharsets.UTF_8)
                .build();

        Exception exception = decoder.decode("methodKey", response);

        Assertions.assertThat(exception).isInstanceOf(CommonHttpException.class);
        CommonHttpException commonEx = (CommonHttpException) exception;
        Assertions.assertThat(commonEx.getStatusCode()).isEqualTo(400);
        Assertions.assertThat(commonEx.getMessage()).isEqualTo(responseBody);
    }

    @Test
    @DisplayName("Body 없을 때 기본 메세지 출력")
    void decode_shouldHandleIOExceptionGracefully() {

        Response response = Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .request(Request.create(Request.HttpMethod.GET, "/api/test", Collections.emptyMap(), null, null, null))
                .body((byte[]) null)
                .build();

        Exception exception = decoder.decode("methodKey", response);

        Assertions.assertThat(exception).isInstanceOf(CommonHttpException.class);
        CommonHttpException commonEx = (CommonHttpException) exception;
        Assertions.assertThat(commonEx.getStatusCode()).isEqualTo(500);
        Assertions.assertThat(commonEx.getMessage()).isEqualTo("Feign 에러 발생");
    }
}