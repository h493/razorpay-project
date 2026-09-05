package com.himanshu.razorpay.payment_service.gateway.dto;


import com.himanshu.razorpay.common_library.entity.Money;
import com.himanshu.razorpay.common_library.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentRequest(
        UUID paymentId,
        UUID orderId,
        UUID merchantId,
        Money amount,
        PaymentMethod method,
        Map<String, Object> methodDetails
) {
}
