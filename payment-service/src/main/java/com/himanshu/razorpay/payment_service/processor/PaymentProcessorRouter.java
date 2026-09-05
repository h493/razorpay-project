package com.himanshu.razorpay.payment_service.processor;


import com.himanshu.razorpay.common_library.enums.PaymentMethod;
import com.himanshu.razorpay.common_library.dto.PaymentProcessorRequest;
import com.himanshu.razorpay.common_library.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentProcessorRouter {

    private final Map<PaymentMethod, PaymentProcessor> paymentProcessorMap;

    public PaymentProcessorResponse charge(PaymentProcessorRequest request){
        PaymentProcessor processor = paymentProcessorMap.get(request.method());
        if(processor == null){
            throw new IllegalArgumentException("No payment processor registered for method: " + request.method());
        }

        return processor.charge(request);
    }
}
