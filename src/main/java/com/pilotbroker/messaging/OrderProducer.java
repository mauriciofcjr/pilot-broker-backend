package com.pilotbroker.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publicar(Long orderId) {
        rabbitTemplate.convertAndSend(
            RabbitMqConfig.EXCHANGE,
            RabbitMqConfig.ROUTING_KEY,
            orderId
        );
    }
}
