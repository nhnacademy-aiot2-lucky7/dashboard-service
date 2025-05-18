package com.nhnacademy.common.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RabbitConfigTest {

    private RabbitConfig rabbitConfig;

    @BeforeEach
    void setUp() {
        rabbitConfig = new RabbitConfig();

        // reflection으로 @Value 주입 대체
        var eventExchangeField = RabbitConfig.class.getDeclaredFields()[0];
        eventExchangeField.setAccessible(true);
        try {
            eventExchangeField.set(rabbitConfig, "test.exchange");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("jackson2JsonMessageConverter Bean 생성 테스트")
    void testJackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = rabbitConfig.jackson2JsonMessageConverter();
        assertThat(converter).isNotNull();
    }

    @Test
    @DisplayName("rabbitTemplate Bean 생성 테스트")
    void testRabbitTemplate() {
        ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        RabbitTemplate template = rabbitConfig.rabbitTemplate(connectionFactory, converter);

        assertThat(template).isNotNull();
        assertThat(template.getMessageConverter()).isEqualTo(converter);
    }

    @Test
    @DisplayName("eventExchange Bean 생성 테스트")
    void testEventExchange() {
        DirectExchange exchange = rabbitConfig.eventExchange();
        assertThat(exchange).isNotNull();
        assertThat(exchange.getName()).isEqualTo("test.exchange");
    }
}