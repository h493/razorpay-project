package com.himanshu.razorpay.payment_service.service.impl;

import com.himanshu.razorpay.common_library.enums.PaymentStatus;
import com.himanshu.razorpay.payment_service.api.PaymentLookupService;
import com.himanshu.razorpay.payment_service.entity.Payment;
import com.himanshu.razorpay.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentLookupServiceImpl implements PaymentLookupService {

    private final PaymentRepository paymentRepository;

    @Override
    public List<Payment> findUnsettlementCapturedPayments(UUID merchantId) {
        return paymentRepository.findByMerchantIdAndStatusForUpdate(merchantId, PaymentStatus.CAPTURED);
    }
}
