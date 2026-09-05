package com.himanshu.razorpay.payment_service.gateway;


import com.himanshu.razorpay.common_library.enums.PaymentMethod;
import com.himanshu.razorpay.payment_service.gateway.dto.PaymentRequest;
import com.himanshu.razorpay.payment_service.gateway.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentGatewayRouter {

    private final Map<PaymentMethod, PaymentAdapter> paymentAdapterMap;

    public PaymentResult initiate(PaymentRequest paymentRequest){
        PaymentAdapter paymentAdapter = paymentAdapterMap.get(paymentRequest.method());
        if(paymentAdapter == null){
            throw new IllegalArgumentException("No Payment Adapter Register for method: " + paymentRequest.method());
        }
        return paymentAdapter.initiate(paymentRequest);
    }

    public PaymentResult capture(PaymentMethod method, UUID paymentId) {
        PaymentAdapter paymentAdapter = paymentAdapterMap.get(method);
        if(paymentAdapter == null){
            throw new IllegalArgumentException("No Payment Adapter Register for method: " + method);
        }
       return paymentAdapter.capture(paymentId);
    }
}
