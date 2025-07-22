package br.com.rinha.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PaymentsSummary {

    @JsonProperty("default")
    private Summary defaultProcessor;
    private Summary fallback;


    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Summary {
        private Integer totalRequests;
        private BigDecimal totalAmount;
    }
}
