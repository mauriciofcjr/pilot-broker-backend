package com.pilotbroker.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String QUEUE       = "trade.orders.queue";
    public static final String EXCHANGE    = "trade.exchange";
    public static final String ROUTING_KEY = "trade.orders";

    @Bean
    public Queue tradeOrdersQueue() {
        return new Queue(QUEUE, true); // durable
    }

    @Bean
    public DirectExchange tradeExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding binding(Queue tradeOrdersQueue, DirectExchange tradeExchange) {
        return BindingBuilder.bind(tradeOrdersQueue).to(tradeExchange).with(ROUTING_KEY);
    }
}
