package com.himanshu.razorpay.payment_service.api;



import com.himanshu.razorpay.payment_service.entity.Payment;

import java.util.List;
import java.util.UUID;

public interface PaymentLookupService {
    List<Payment> findUnsettlementCapturedPayments(UUID merchantId);
}
