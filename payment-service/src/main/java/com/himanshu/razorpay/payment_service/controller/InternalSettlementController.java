package com.himanshu.razorpay.payment_service.controller;

import com.himanshu.razorpay.common_library.dto.PaymentSettlementView;
import com.himanshu.razorpay.payment_service.api.PaymentLookupService;
import com.himanshu.razorpay.payment_service.service.impl.PaymentLookupServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/payments")
public class InternalSettlementController {

    private final PaymentLookupService paymentLookupService;

    @GetMapping("/unsettled-captured")
    public List<PaymentSettlementView> findUnsettledCaptured(@RequestParam UUID merchantId) {
        return paymentLookupService.findUnsettlementCapturedPayments(merchantId);
    }

    @PostMapping("/mark-settled")
    public void markSettled(@RequestBody List<UUID> paymentIds) {
        paymentLookupService.markPaymentsAsSettled(paymentIds);
    }
}
