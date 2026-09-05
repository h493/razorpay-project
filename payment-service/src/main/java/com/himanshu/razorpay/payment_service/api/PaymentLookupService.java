package com.himanshu.razorpay.payment_service.api;



import com.himanshu.razorpay.common_library.dto.PaymentSettlementView;
import com.himanshu.razorpay.payment_service.entity.Payment;

import java.util.List;
import java.util.UUID;

public interface PaymentLookupService {
    List<PaymentSettlementView> findUnsettlementCapturedPayments(UUID merchantId);

    void markPaymentsAsSettled(List<UUID> paymentIds);
}
