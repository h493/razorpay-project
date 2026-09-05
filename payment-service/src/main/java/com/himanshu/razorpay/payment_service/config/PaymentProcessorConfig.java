package com.himanshu.razorpay.payment_service.config;

import com.himanshu.razorpay.common_library.enums.PaymentMethod;
import com.himanshu.razorpay.payment_service.processor.PaymentProcessor;
import com.himanshu.razorpay.payment_service.processor.strategy.CardPaymentProcessor;
import com.himanshu.razorpay.payment_service.processor.strategy.NetBankingPaymentProcessor;
import com.himanshu.razorpay.payment_service.processor.strategy.UPIPaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentProcessorConfig {

    private final CardPaymentProcessor cardPaymentProcessor;
    private final NetBankingPaymentProcessor netBankingPaymentProcessor;
    private final UPIPaymentProcessor upiPaymentProcessor;

    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessorMap(){
        return Map.of(
                PaymentMethod.CARD, cardPaymentProcessor,
                PaymentMethod.NET_BANKING, netBankingPaymentProcessor,
                PaymentMethod.UPI, upiPaymentProcessor
        );
    }
}
