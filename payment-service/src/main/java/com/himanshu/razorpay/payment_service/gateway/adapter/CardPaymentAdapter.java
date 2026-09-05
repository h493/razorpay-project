package com.himanshu.razorpay.payment_service.gateway.adapter;


import com.himanshu.razorpay.payment_service.gateway.PaymentAdapter;
import com.himanshu.razorpay.payment_service.gateway.dto.PaymentRequest;
import com.himanshu.razorpay.payment_service.gateway.dto.PaymentResult;
import com.himanshu.razorpay.payment_service.processor.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardPaymentAdapter implements PaymentAdapter {

    private final VaultService vaultService;

    @Override
    public PaymentResult initiate(PaymentRequest request){
        String token = (String) request.methodDetails().get("token");

        PaymentProcessorResponse response = vaultService.charge(token, request.paymentId(), request.amount(), request.methodDetails());

        return switch (response){
            case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
            case PaymentProcessorResponse.Failure failure -> new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
            case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorReference());
        };
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("CARD_REF");
    }
}
