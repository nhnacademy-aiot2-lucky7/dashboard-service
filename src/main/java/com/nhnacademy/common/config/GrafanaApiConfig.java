package com.nhnacademy.common.config;

import feign.RequestInterceptor;
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
}
