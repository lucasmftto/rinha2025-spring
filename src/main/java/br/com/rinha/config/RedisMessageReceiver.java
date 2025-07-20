package br.com.rinha.config;

import br.com.rinha.dto.TransactionResource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

@Service
public class RedisMessageReceiver {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void receiveMessage(String message) throws JsonProcessingException {
        System.out.println("Received message: " + message);
        message = formatMessage(message);
        TransactionResource transaction = objectMapper.readValue(message, TransactionResource.class);

        transaction.setRequestedAt(LocalDateTime.now());
        // Salvar transação
        long timestamp = transaction.getRequestedAt().toEpochSecond(ZoneOffset.UTC); // supondo que tem campo data
        redisTemplate.opsForZSet().add("transactions", objectMapper.writeValueAsString(transaction), timestamp);

// Buscar por data
        double fromTimestamp = LocalDateTime.parse("2025-07-29T00:00:00").toEpochSecond(ZoneOffset.UTC);
        double toTimestamp = LocalDateTime.parse("2025-07-31T23:59:59").toEpochSecond(ZoneOffset.UTC);
        Set<Object> results = redisTemplate.opsForZSet().rangeByScore("transactions", fromTimestamp, toTimestamp);

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
