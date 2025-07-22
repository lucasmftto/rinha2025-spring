package br.com.rinha.controller;

import br.com.rinha.service.PaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdmController {

    @Autowired
    private PaymentTransactionService service;

    @PostMapping(path = "/purge-payments", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Void> clearDB() {
        this.service.purgeTransactions();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
