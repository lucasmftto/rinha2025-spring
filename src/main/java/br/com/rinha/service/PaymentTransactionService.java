package br.com.rinha.service;

import br.com.rinha.config.external.PaymentProcessorDefaultClient;
import br.com.rinha.config.external.PaymentProcessorFallbackClient;
import br.com.rinha.dto.PaymentsSummary;
import br.com.rinha.dto.TransactionResource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PaymentTransactionService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentProcessorDefaultClient defaultClient;

    @Autowired
    private PaymentProcessorFallbackClient fallbackClient;

    public void processTransaction(TransactionResource transaction) throws JsonProcessingException {
        // TODO Add resquest for payment processor
        transaction.setRequestedAt(LocalDateTime.now());
        transaction.setProcessorBy("default");

        try {
            defaultClient.processPayment(transaction);
            this.saveTransaction(transaction, "default");
        } catch (Exception e) {
            // Se falhar, processa com o fallback
            transaction.setProcessorBy("fallback");
            fallbackClient.processPayment(transaction);
            this.saveTransaction(transaction, "fallback");
        }

    }

    private void saveTransaction(TransactionResource transaction, String destination) throws JsonProcessingException {

        // Salvar transação
        long timestamp = transaction.getRequestedAt().toEpochSecond(ZoneOffset.UTC);
        redisTemplate.opsForZSet().add("transactions_" + destination,
                objectMapper.writeValueAsString(transaction), timestamp);
    }

    public void purgeTransactions() {
        redisTemplate.delete("transactions_default");
        redisTemplate.delete("transactions_fallback");
        redisTemplate.delete("transactions");
    }

    public PaymentsSummary getTransactionsByDateRange(LocalDateTime from, LocalDateTime to) {
        double fromTimestamp = from.toEpochSecond(ZoneOffset.UTC);
        double toTimestamp = to.toEpochSecond(ZoneOffset.UTC);
        Set<Object> resultsDefault = redisTemplate.opsForZSet()
                .rangeByScore("transactions_default", fromTimestamp, toTimestamp);
        Set<Object> resultsFallback = redisTemplate.opsForZSet()
                .rangeByScore("transactions_fallback", fromTimestamp, toTimestamp);


        // Otimização: processa e soma em uma única passagem
//        BigDecimal totalAmountDefault = BigDecimal.ZERO;
//        int countDefault = 0;
//        BigDecimal totalAmountFallback = BigDecimal.ZERO;
//        int countFallback = 0;


        BigDecimal sumDefault = resultsDefault.stream()
                .map(i -> {
                    try {
                        return objectMapper.readValue((String) i, TransactionResource.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }).map(TransactionResource::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sumFallback = resultsFallback.stream()
                .map(i -> {
                    try {
                        return objectMapper.readValue((String) i, TransactionResource.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }).map(TransactionResource::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


//        for (Object obj : results) {
//            try {
//                TransactionResource tr = objectMapper.readValue((String) obj, TransactionResource.class);
//                if ("default".equals(tr.getProcessorBy())) {
//                    totalAmountDefault = totalAmountDefault.add(tr.getAmount());
//                    countDefault++;
//                } else if ("fallback".equals(tr.getProcessorBy())) {
//                    totalAmountFallback = totalAmountFallback.add(tr.getAmount());
//                    countFallback++;
//                }
//            } catch (JsonProcessingException e) {
//                // Loga ou ignora
//            }
//        }

        PaymentsSummary paymentsSummary = PaymentsSummary.builder()
                .defaultProcessor(PaymentsSummary.Summary.builder()
                        .totalAmount(sumDefault)
                        .totalRequests(resultsDefault.size()).build())
                .fallback(PaymentsSummary.Summary.builder()
                        .totalAmount(sumFallback)
                        .totalRequests(resultsFallback.size()).build())
                .build();

        return paymentsSummary;
    }
}
