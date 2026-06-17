package com.apex.clear_engine.infrastructure.configuration;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "apex.transfer.events";
    public static final String QUEUE_NOTIFICATIONS = "transfer.notifications";
    public static final String ROUTING_KEY = "transfer.completed";

    @Bean
    public TopicExchange transferExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue notificationsQueue() {
        return new Queue(QUEUE_NOTIFICATIONS, true);
    }

    @Bean
    public Binding bindingNotifications(Queue notificationsQueue, TopicExchange transferExchange) {
        return BindingBuilder
                .bind(notificationsQueue)
                .to(transferExchange)
                .with(ROUTING_KEY);
    }
}
