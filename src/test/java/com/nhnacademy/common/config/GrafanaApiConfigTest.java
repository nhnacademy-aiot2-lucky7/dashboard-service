package com.nhnacademy.common.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

class GrafanaApiConfigTest {

    @Test
    @DisplayName("RequestInterceptor가 Authorization 헤더를 추가하는지 확인")
    void requestInterceptorAddsAuthorizationHeader() throws Exception {

        String expectedApiKey = "test-api-key";
        GrafanaApiConfig config = new GrafanaApiConfig();

        // @Value 주입을 대신하여 리플렉션으로 필드 주입
        Field apiKeyField = GrafanaApiConfig.class.getDeclaredField("apiKey");
        apiKeyField.setAccessible(true);
        apiKeyField.set(config, expectedApiKey);

        RequestInterceptor interceptor = config.requestInterceptor();
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        Assertions.assertThat(template.headers())
                .containsKey("Authorization");
        Assertions.assertThat(template.headers().get("Authorization"))
                .containsExactly("Bearer " + expectedApiKey);
    }
}