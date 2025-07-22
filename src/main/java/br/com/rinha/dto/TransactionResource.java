package br.com.rinha.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TransactionResource implements Serializable {

    private String correlationId;
    private BigDecimal amount;
    private LocalDateTime requestedAt;
    private String processorBy;
}
