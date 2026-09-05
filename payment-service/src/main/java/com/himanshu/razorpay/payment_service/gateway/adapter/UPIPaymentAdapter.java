package com.himanshu.razorpay.payment_service.gateway.adapter;


import com.himanshu.razorpay.common_library.enums.PaymentMethod;
import com.himanshu.razorpay.payment_service.gateway.PaymentAdapter;
import com.himanshu.razorpay.payment_service.gateway.dto.PaymentRequest;
import com.himanshu.razorpay.payment_service.gateway.dto.PaymentResult;
import com.himanshu.razorpay.payment_service.processor.PaymentProcessorRouter;
import com.himanshu.razorpay.common_library.dto.PaymentProcessorRequest;
import com.himanshu.razorpay.common_library.dto.PaymentProcessorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class UPIPaymentAdapter implements PaymentAdapter {

    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    public PaymentResult initiate(PaymentRequest request){
        log.info("Initiate Payment with UPIPaymentAdapter, paymentId: {}", request.paymentId());

        try {
            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.nonCard(
                    request.paymentId(), PaymentMethod.UPI, request.amount(), request.methodDetails()
            );

            PaymentProcessorResponse paymentProcessorResponse = paymentProcessorRouter.charge(paymentProcessorRequest);

            return switch (paymentProcessorResponse) {
                case PaymentProcessorResponse.Failure failure ->
                        new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());

                case PaymentProcessorResponse.Pending pending ->
                        new PaymentResult.Pending(pending.processorReference());

                case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
            };
        } catch (Exception e) {
            log.warn("UPI failed, paymentId: {}", request.paymentId());
            return new PaymentResult.Failure("UPI_FAILED", e.getMessage());
        }
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("UPI_REF");
    }
}
