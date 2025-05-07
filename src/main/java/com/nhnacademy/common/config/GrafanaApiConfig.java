package com.nhnacademy.common.config;

import com.nhnacademy.common.decoder.FeignErrorDecoder;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrafanaApiConfig {

    @Value("${grafana.api-key}")
    private String apiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> template.header("Authorization", "Bearer " + apiKey);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}
