package com.himanshu.razorpay.payment_service.processor;


import com.himanshu.razorpay.payment_service.processor.dto.PaymentProcessorRequest;
import com.himanshu.razorpay.payment_service.processor.dto.PaymentProcessorResponse;

public interface PaymentProcessor {

    PaymentProcessorResponse charge(PaymentProcessorRequest request);
}
