package com.himanshu.razorpay.payment_service.client;

import com.himanshu.razorpay.common_library.dto.PaymentProcessorResponse;
import com.himanshu.razorpay.common_library.dto.VaultChargeRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "vault-service", path = "/internal/vault")
public interface VaultServiceClient {

    @PostMapping("/charge")
    PaymentProcessorResponse  charge(@RequestBody VaultChargeRequest request);
}
