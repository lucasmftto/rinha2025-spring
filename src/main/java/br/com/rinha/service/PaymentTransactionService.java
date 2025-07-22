package br.com.rinha.service;

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

    public void processTransaction(TransactionResource transaction) throws JsonProcessingException {
        // TODO Add resquest for payment processor

        this.saveTransaction(transaction);
    }

    private void saveTransaction(TransactionResource transaction) throws JsonProcessingException {
        transaction.setRequestedAt(LocalDateTime.now());
        transaction.setProcessorBy("default");
        // Salvar transação
        long timestamp = transaction.getRequestedAt().toEpochSecond(ZoneOffset.UTC);
        redisTemplate.opsForZSet().add("transactions", objectMapper.writeValueAsString(transaction), timestamp);
    }

    public void purgeTransactions() {
        redisTemplate.delete("transactions");
    }

    public PaymentsSummary getTransactionsByDateRange(LocalDateTime from, LocalDateTime to) {
        double fromTimestamp = from.toEpochSecond(ZoneOffset.UTC);
        double toTimestamp = to.toEpochSecond(ZoneOffset.UTC);
        Set<Object> results = redisTemplate.opsForZSet().rangeByScore("transactions", fromTimestamp, toTimestamp);

        List<TransactionResource> list = results.stream()
                .map(obj -> {

                    try {
                        return objectMapper.readValue((String) obj, TransactionResource.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                })
                .collect(Collectors.toList());

        List<TransactionResource> aDefault =
                list.stream().filter(i -> i.getProcessorBy().equals("default")).collect(Collectors.toList());
        BigDecimal totalAmountDefault = aDefault.stream()
                .map(TransactionResource::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        List<TransactionResource> aFallback =
                list.stream().filter(i -> i.getProcessorBy().equals("fallback")).collect(Collectors.toList());
        BigDecimal totalAmountFallback = aFallback.stream()
                .map(TransactionResource::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PaymentsSummary paymentsSummary = PaymentsSummary.builder()
                .defaultProcessor(PaymentsSummary.Summary.builder().totalAmount(totalAmountDefault).totalRequests(aDefault.size()).build())
                .fallback(PaymentsSummary.Summary.builder().totalAmount(totalAmountFallback).totalRequests(aFallback.size()).build())
                .build();


        return paymentsSummary;
    }
}
