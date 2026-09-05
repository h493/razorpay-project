package com.himanshu.razorpay.vault_service.processor;

import com.himanshu.razorpay.common_library.dto.PaymentProcessorResponse;
import com.himanshu.razorpay.common_library.dto.PaymentProcessorRequest;
import com.himanshu.razorpay.common_library.util.RandomizerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CardPaymentProcessor {

    public static final String PAN_CARD_DECLINED = "40000000000002";
    public static final String PAN_CARD_EXPIRED = "4000003200000002";


    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {

        String pan = request.pan();

        if(PAN_CARD_DECLINED.equals(pan)){
            log.warn("Card Declined");
            return new PaymentProcessorResponse.Failure("CARD_DECLINED", "Card Declined by Bank");
        }

        if(PAN_CARD_EXPIRED.equals(pan)){
            log.warn("Pan card has expired");
            return new PaymentProcessorResponse.Failure("CARD_EXPIRED", "Card has Expired");
        }

        String processorReference = "CARD_PROCESSOR" + RandomizerUtil.randomBase64(16);
        return new PaymentProcessorResponse.Pending(processorReference);
    }
}
