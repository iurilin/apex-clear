package com.apex.clear_engine.infrastructure.messaging;

import com.apex.clear_engine.infrastructure.configuration.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TransferNotificationConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATIONS)
    public void consumeTransferEvent(String messageText) {
        try {
            System.out.println("\n📥 [RABBITMQ] Nova mensagem capturada da fila: " + messageText);

            Map<String, String> data = parseMessage(messageText);

            String id = data.get("id");
            String origem = data.get("origem");
            String destino = data.get("destino");
            String valor = data.get("valor");

            System.out.println("====== 🔔 SISTEMA DE NOTIFICAÇÕES (ASSÍNCRONO) ======");
            System.out.println("-> ID da Transação: " + id);
            System.out.println("-> Notificando conta de ORIGEM (" + origem + "): Débito de R$ " + valor + " realizado com sucesso.");
            System.out.println("-> Notificando conta de DESTINO (" + destino + "): Crédito de R$ " + valor + " recebido.");
            System.out.println("=====================================================\n");

        } catch (Exception e) {
            System.err.println("❌ Erro ao processar mensagem da fila: " + e.getMessage());
        }
    }

    private Map<String, String> parseMessage(String messageText) {
        Map<String, String> map = new HashMap<>();
        String[] tokens = messageText.split(",");
        for (String token : tokens) {
            String[] keyValue = token.split(":", 2);
            if (keyValue.length == 2) {
                map.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return map;
    }
}
