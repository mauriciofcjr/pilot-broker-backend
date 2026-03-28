package com.pilotbroker.messaging;

import com.pilotbroker.model.OrderStatus;
import com.pilotbroker.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {

    private final OrderRepository orderRepository;

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    public void processar(Long orderId) {
        log.info("Processando ordem id={}", orderId);
        orderRepository.findById(orderId).ifPresentOrElse(order -> {
            order.setStatus(OrderStatus.PROCESSED);
            orderRepository.save(order);
            log.info("Ordem id={} processada com sucesso", orderId);
        }, () -> log.warn("Ordem id={} não encontrada para processamento", orderId));
    }
}
