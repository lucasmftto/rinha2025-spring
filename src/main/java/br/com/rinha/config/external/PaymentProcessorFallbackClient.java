package br.com.rinha.config.external;

import br.com.rinha.dto.TransactionResource;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PaymentProcessorFallback", url = "${payment.processor.fallback.url:http://localhost:8002}",
        configuration = FeignClientConfig.class)
public interface PaymentProcessorFallbackClient {

    @PostMapping("/payments")
    String processPayment(@RequestBody TransactionResource transactionResource);
}
