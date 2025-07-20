package br.com.rinha.controller;

import br.com.rinha.dto.TransactionResource;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping
public class PaymentController {

    @PostMapping(path = "/payments", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> newTransaction(@RequestBody @Valid TransactionResource transactionResource) {

        System.out.println("Thread: " + Thread.currentThread().getName() + " - " + LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/payments-summary")
    public ResponseEntity<Void> getPaymentsSummary(@RequestParam(name = "from") String from,
                                                   @RequestParam(name = "to") String to) {
        System.out.println("Summary!!!!");
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
