package com.nhnacademy.common.decoder;

import com.nhnacademy.common.exception.CommonHttpException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class FeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        String message = "Feign 에러 발생";

        if (response.body() != null) {
            try (InputStream is = response.body().asInputStream()) {
                message = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                message = "응답 바디 읽기 실패";
            }
        }
        return new CommonHttpException(response.status(), message);
    }
}