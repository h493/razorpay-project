package com.himanshu.razorpay.payment_service.processor;


import com.himanshu.razorpay.common_library.dto.PaymentProcessorRequest;
import com.himanshu.razorpay.common_library.dto.PaymentProcessorResponse;

public interface PaymentProcessor {

    PaymentProcessorResponse charge(PaymentProcessorRequest request);
}
