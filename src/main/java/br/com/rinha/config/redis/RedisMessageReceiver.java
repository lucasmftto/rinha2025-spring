package br.com.rinha.config.redis;

import br.com.rinha.dto.TransactionResource;
import br.com.rinha.service.PaymentTransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
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

//    @PostConstruct
//    public void startQueueConsumer() {
//        new Thread(() -> {
//            while (true) {
//                try {
//                    // BRPOP bloqueante, timeout 0 = espera indefinidamente
//                    Object message = redisTemplate.opsForList().rightPop("payments-queue", 0, java.util.concurrent.TimeUnit.SECONDS);
//                    if (message != null) {
//                        String msgStr = message.toString();
//                        msgStr = formatMessage(msgStr);
//                        TransactionResource transaction = objectMapper.readValue(msgStr, TransactionResource.class);
//                        this.service.processTransaction(transaction);
////                        System.out.println("Transaction consumed: " + transaction);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }, "redis-queue-consumer").start();
//    }

    @PostConstruct
    public void startQueueConsumers() {
        int numThreads = 5; // Defina conforme sua necessidade e recursos
        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                while (true) {
                    try {
                        Object message = redisTemplate.opsForList().rightPop("payments-queue", 0, java.util.concurrent.TimeUnit.SECONDS);
                        if (message != null) {
                            String msgStr = message.toString();
                            msgStr = formatMessage(msgStr);
                            TransactionResource transaction = objectMapper.readValue(msgStr, TransactionResource.class);
                            this.service.processTransaction(transaction);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, "redis-consumer-" + i).start();
        }
    }

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
