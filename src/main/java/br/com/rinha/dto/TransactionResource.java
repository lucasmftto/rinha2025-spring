package br.com.rinha.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransactionResource {

    private String correlationId;
    private BigDecimal amount;
}
