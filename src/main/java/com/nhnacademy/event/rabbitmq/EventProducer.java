package com.nhnacademy.event.rabbitmq;

import com.nhnacademy.event.event.EventCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${mq.exchange.event}")
    private String exchange;

    @Value("${mq.routing-key.event}")
    private String routingKey;

    public void sendEvent(EventCreateRequest request) {
        rabbitTemplate.convertAndSend(exchange, routingKey, request);
    }
}
