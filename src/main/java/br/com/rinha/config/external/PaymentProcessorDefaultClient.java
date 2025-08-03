package br.com.rinha.config.external;

import br.com.rinha.dto.TransactionResource;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PaymentProcessorDefault", url = "${payment.processor.default.url:http://localhost:8001}",
        configuration = FeignClientConfig.class)
public interface PaymentProcessorDefaultClient {

    @PostMapping("/payments")
    String processPayment(@RequestBody TransactionResource transactionResource);
}
