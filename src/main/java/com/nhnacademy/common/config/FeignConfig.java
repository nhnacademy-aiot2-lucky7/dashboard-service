package com.nhnacademy.common.config;

import com.nhnacademy.common.decoder.FeignErrorDecoder;
import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }


    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(10000, 10000); // ms 단위
    }
}
