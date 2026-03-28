package com.pilotbroker.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publicar(Long orderId) {
        log.info("Publicando ordem id={} na fila", orderId);
        rabbitTemplate.convertAndSend(
            RabbitMqConfig.EXCHANGE,
            RabbitMqConfig.ROUTING_KEY,
            orderId
        );
    }
}
