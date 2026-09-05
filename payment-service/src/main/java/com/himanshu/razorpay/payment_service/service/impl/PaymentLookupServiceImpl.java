package com.himanshu.razorpay.payment_service.service.impl;

import com.himanshu.razorpay.common_library.dto.PaymentSettlementView;
import com.himanshu.razorpay.common_library.enums.PaymentStatus;
import com.himanshu.razorpay.payment_service.api.PaymentLookupService;
import com.himanshu.razorpay.payment_service.entity.Payment;
import com.himanshu.razorpay.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentLookupServiceImpl implements PaymentLookupService {

    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public List<PaymentSettlementView> findUnsettlementCapturedPayments(UUID merchantId) {
        List<Payment> payments = paymentRepository.findByMerchantIdAndStatusForUpdate(merchantId, PaymentStatus.CAPTURED);
            return payments.stream()
                    .map(payment -> new PaymentSettlementView(
                            payment.getId(),
                            payment.getAmount().getAmountUnits(),
                            0,
                            payment.getAmount().getCurrency()
                    ))
                    .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markPaymentsAsSettled(List<UUID> paymentIds) {
        if (paymentIds == null || paymentIds.isEmpty()) {
            log.warn("No payment IDs provided for settlement marking");
            return;
        }
        List<Payment> payments = paymentRepository.findAllById(paymentIds);
        for (Payment payment : payments) {
            payment.setStatus(PaymentStatus.SETTLED);
            payment.setSettledAt(LocalDateTime.now());
        }
        paymentRepository.saveAll(payments);
    }
}
