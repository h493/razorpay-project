package com.himanshu.razorpay.payment_service.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.himanshu.razorpay.common_library.entity.Money;
import com.himanshu.razorpay.common_library.enums.PaymentMethod;
import com.himanshu.razorpay.common_library.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(
        UUID id,
        UUID orderId,
        UUID merchantId,
        Money money,
        PaymentStatus status,
        PaymentMethod method,
        Map<String, Object> methodDetails,
        String errorCode,
        String errorDescription,
        LocalDateTime capturedAt,
        LocalDateTime createdAt
) {
}
