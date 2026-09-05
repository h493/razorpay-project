package com.himanshu.razorpay.payment_service.processor.dto;


import com.himanshu.razorpay.common_library.entity.Money;
import com.himanshu.razorpay.common_library.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentProcessorRequest(
        UUID paymentId,
        UUID processingId,
        PaymentMethod method,
        Money amount,
        String pan,
        String expiry,
        Map<String,Object> methodDetails
) {

    public static PaymentProcessorRequest card(UUID paymentId, String pan, String expiry, Money amount,
                                               Map<String, Object> methodDetails) {
        return new PaymentProcessorRequest(paymentId, UUID.randomUUID(), PaymentMethod.CARD, amount, pan, expiry, methodDetails);
    }

    public static PaymentProcessorRequest nonCard(UUID paymentId, PaymentMethod method, Money amount,
                                               Map<String, Object> methodDetails) {
        return new PaymentProcessorRequest(paymentId, UUID.randomUUID(), method, amount, null, null, methodDetails);
    }
}
