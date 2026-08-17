package com.apex.clear_engine.infrastructure.messaging;

import com.apex.clear_engine.infrastructure.configuration.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TransferNotificationConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATIONS)
    public void consumeTransferEvent(String messageText) {
        System.out.println("\n📥 [RABBITMQ] Tentando processar mensagem: " + messageText);

        System.out.println("❌ [ERRO SIMULADO] Falha crítica na integração de notificações!");
        throw new RuntimeException("Simulação: Serviço de notificações indisponível.");
    }
}
