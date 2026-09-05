package com.himanshu.razorpay.vault_service.service;

import com.himanshu.razorpay.common_library.entity.Money;
import com.himanshu.razorpay.vault_service.dto.request.TokenizeRequest;
import com.himanshu.razorpay.vault_service.dto.response.TokenizeResponse;

import java.util.Map;
import java.util.UUID;

public interface VaultService {
    TokenizeResponse tokenize(TokenizeRequest request, UUID merchantId);

    PaymentProcessorResponse charge(String token, UUID paymentId, Money amount, Map<String, Object> methodDetails);
}
