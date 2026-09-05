package com.himanshu.razorpay.payment_service.processor.strategy;

import com.himanshu.razorpay.common_library.util.RandomizerUtil;
import com.himanshu.razorpay.payment_service.processor.PaymentProcessor;
import com.himanshu.razorpay.payment_service.processor.dto.PaymentProcessorRequest;
import com.himanshu.razorpay.payment_service.processor.dto.PaymentProcessorResponse;
import org.springframework.stereotype.Component;

@Component
public class UPIPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {

        final String UPI_CODE_FAIL = "fail@okaxis";

        String bankCode = request.methodDetails() != null ? request.methodDetails().get("vpa").toString() : null;

        //simulation
        if(UPI_CODE_FAIL.equals(bankCode)){
            return new PaymentProcessorResponse.Failure("UPI_REJECTED",
                    "Bank rejected the transaction registration");
        }

        String processorReference = "UPI_PROCESSOR" + RandomizerUtil.randomBase64(16);

//        String redirectReference = "BANK_REF" + processorReference;

        return new PaymentProcessorResponse.Pending(processorReference);
    }
}
