package com.apex.clear_engine.infrastructure.configuration;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "apex.transfer.events";
    public static final String QUEUE_NOTIFICATIONS = "transfer.notifications";
    public static final String ROUTING_KEY = "transfer.completed";

    public static final String DLX_NAME = "apex.transfer.dlx";
    public static final String QUEUE_NOTIFICATIONS_DLQ = "transfer.notifications.dlq";
    public static final String DLQ_ROUTING_KEY = "transfer.notifications.dead";

    @Bean
    public TopicExchange transferExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_NAME);
    }

    @Bean
    public Queue notificationsDlq() {
        return new Queue(QUEUE_NOTIFICATIONS_DLQ, true);
    }

    @Bean
    public Binding bindingNotificationsDlq(Queue notificationsDlq, DirectExchange deadLetterExchange) {
        return BindingBuilder
                .bind(notificationsDlq)
                .to(deadLetterExchange)
                .with(DLQ_ROUTING_KEY);
    }

    @Bean
    public Queue notificationsQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_NAME);
        args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY);
        return new Queue(QUEUE_NOTIFICATIONS, true, false, false, args);
    }

    @Bean
    public Binding bindingNotifications(Queue notificationsQueue, TopicExchange transferExchange) {
        return BindingBuilder
                .bind(notificationsQueue)
                .to(transferExchange)
                .with(ROUTING_KEY);
    }
}
