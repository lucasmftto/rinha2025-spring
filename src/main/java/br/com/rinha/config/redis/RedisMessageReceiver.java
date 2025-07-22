package br.com.rinha.config.redis;

import br.com.rinha.dto.TransactionResource;
import br.com.rinha.service.PaymentTransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisMessageReceiver {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PaymentTransactionService service;

    public void receiveMessage(String message) throws JsonProcessingException {
        System.out.println("Received message: " + message);
        message = formatMessage(message);
        TransactionResource transaction = objectMapper.readValue(message, TransactionResource.class);

        this.service.processTransaction(transaction);

        System.out.println("Transaction received: " + transaction);
    }

    private static String formatMessage(String message) {
        if (message.startsWith("\"") && message.endsWith("\"")) {
            message = message.substring(1, message.length() - 1);
        }
        message = message.replace("\\", "");
        return message;
    }
}
