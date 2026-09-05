package com.himanshu.razorpay.common_library.dto;

import com.himanshu.razorpay.common_library.entity.Money;

import java.util.Map;
import java.util.UUID;

public record VaultChargeRequest(
        UUID paymentId,
        String token,
        Money amount,
        Map<String, Object> methodDetails
) {
}
