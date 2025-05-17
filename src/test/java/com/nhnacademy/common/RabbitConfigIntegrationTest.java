package com.nhnacademy.common;

import com.nhnacademy.common.config.RabbitConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(RabbitConfig.class)
@SpringBootTest
@ActiveProfiles("test")
class RabbitConfigIntegrationTest {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    Jackson2JsonMessageConverter messageConverter;

    @Autowired
    DirectExchange eventExchange;

    @Test
    void contextLoads() {
        Assertions.assertThat(rabbitTemplate).isNotNull();
        Assertions.assertThat(rabbitTemplate.getMessageConverter()).isInstanceOf(Jackson2JsonMessageConverter.class);

        Assertions.assertThat(eventExchange).isNotNull();
        Assertions.assertThat(eventExchange.getName()).isEqualTo("event.exchange");
    }
}

