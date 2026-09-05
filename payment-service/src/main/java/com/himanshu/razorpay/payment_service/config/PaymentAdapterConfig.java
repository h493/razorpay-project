package com.himanshu.razorpay.payment_service.config;


import com.himanshu.razorpay.common_library.enums.PaymentMethod;
import com.himanshu.razorpay.payment_service.gateway.PaymentAdapter;
import com.himanshu.razorpay.payment_service.gateway.adapter.CardPaymentAdapter;
import com.himanshu.razorpay.payment_service.gateway.adapter.NetBankingAdapter;
import com.himanshu.razorpay.payment_service.gateway.adapter.UPIPaymentAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentAdapterConfig {

    private final NetBankingAdapter netBankingAdapter;
    private final CardPaymentAdapter cardPaymentAdapter;
    private final UPIPaymentAdapter upiPaymentAdapter;

    @Bean
    public Map<PaymentMethod, PaymentAdapter> paymentAdapterMap(){
        return Map.of(
                PaymentMethod.CARD, cardPaymentAdapter,
                PaymentMethod.NET_BANKING, netBankingAdapter,
                PaymentMethod.UPI, upiPaymentAdapter
        );
    }
}
