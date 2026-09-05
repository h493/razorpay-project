package com.himanshu.razorpay.payment_service.client;

import com.himanshu.razorpay.common_library.dto.FindOrCreateCustomerRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "merchant-service", path = "/internal/customers")
public interface CustomerServiceClient {

    @PostMapping("find-or-create")
    UUID findOrCreate(@RequestBody FindOrCreateCustomerRequest request);
}
