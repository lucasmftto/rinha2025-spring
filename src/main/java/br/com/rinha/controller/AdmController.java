package br.com.rinha.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdmController {

    @PostMapping(path = "/purge-payments", consumes = "application/json", produces = "application/json")
        public ResponseEntity<Void> clearDB() {

        System.out.println("DB CLEAR!!!" );
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
