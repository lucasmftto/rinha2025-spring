package br.com.rinha.controller;

import br.com.rinha.dto.PaymentsSummary;
import br.com.rinha.dto.TransactionResource;
import br.com.rinha.service.PaymentTransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestController
@RequestMapping
public class PaymentController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String QUEUE_NAME = "payments-queue";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentTransactionService service;

    @PostMapping(path = "/payments", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> newTransaction(@RequestBody @Valid TransactionResource transactionResource) throws JsonProcessingException {
        // Adiciona a mensagem na fila (lista) do Redis ao invés de Pub/Sub
        redisTemplate.opsForList().leftPush(QUEUE_NAME, objectMapper.writeValueAsString(transactionResource));
//        System.out.println("Thread: " + Thread.currentThread().getName() + " - " + LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/payments-summary")
    public ResponseEntity<?> getPaymentsSummary(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {
        System.out.println("Sumary");
        System.out.println("From: " + from);
        System.out.println("To: " + to);

        OffsetDateTime fromDate;
        OffsetDateTime toDate;
        try {
            fromDate = from != null ? parseDate(from) : OffsetDateTime.MIN;
            toDate = to != null ? parseDate(to) : OffsetDateTime.MAX;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        PaymentsSummary transactionsByDateRange =
                this.service.getTransactionsByDateRange(
                        fromDate.toLocalDateTime(),
                        toDate.toLocalDateTime());
        return ResponseEntity.status(HttpStatus.OK).body(transactionsByDateRange);
    }

    private OffsetDateTime parseDate(String dateStr) {
        if (dateStr == null) return null;
        try {
            return OffsetDateTime.parse(dateStr);
        } catch (Exception e) {
            // Tenta LocalDateTime sem offset
            try {
                return LocalDateTime.parse(dateStr).atOffset(ZoneOffset.UTC);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Formato de data inválido. Use ISO 8601, ex: 2000-01-01T00:00:00 ou 2000-01-01T00:00:00Z");
            }
        }
    }
}
