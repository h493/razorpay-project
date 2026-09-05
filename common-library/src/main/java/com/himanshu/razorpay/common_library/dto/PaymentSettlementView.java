package com.himanshu.razorpay.common_library.dto;

import java.util.UUID;

public record PaymentSettlementView(
        UUID paymentId,
        long amountUnits,
        int refundAmountUnits,
        String currency
) {
}
