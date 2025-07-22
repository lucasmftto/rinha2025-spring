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
        redisTemplate.convertAndSend(QUEUE_NAME, objectMapper.writeValueAsString(transactionResource));
        System.out.println("Thread: " + Thread.currentThread().getName() + " - " + LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/payments-summary")
    public ResponseEntity<PaymentsSummary> getPaymentsSummary(@RequestParam(name = "from") String from,
                                                   @RequestParam(name = "to") String to) {

        PaymentsSummary transactionsByDateRange =
                this.service.getTransactionsByDateRange(LocalDateTime.now().minusYears(7),
                        LocalDateTime.now().plusDays(1));
        System.out.println("Summary!!!!");
        return ResponseEntity.status(HttpStatus.OK).body(transactionsByDateRange);
    }
}
