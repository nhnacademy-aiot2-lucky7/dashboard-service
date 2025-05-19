package com.nhnacademy.common.config;

import com.nhnacademy.common.decoder.FeignErrorDecoder;
import feign.codec.ErrorDecoder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FeignConfigTest {

    @Test
    @DisplayName("errorDecoder 메서드는 FeignErrorDecoder 인스턴스를 반환")
    void errorDecoder() {

        FeignConfig feignConfig = new FeignConfig();

        ErrorDecoder errorDecoder = feignConfig.errorDecoder();

        Assertions.assertThat(errorDecoder).isInstanceOf(FeignErrorDecoder.class);
    }
}