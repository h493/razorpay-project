package com.himanshu.razorpay.payment_service.dto.request;

import com.himanshu.razorpay.common_library.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record PaymentInitRequest(
        @NotNull(message = "Order Id is required")
        UUID orderId,

        @NotNull(message = "Payment Method is required")
        PaymentMethod method,
        Map<String, Object> methodDetails
) {
}
