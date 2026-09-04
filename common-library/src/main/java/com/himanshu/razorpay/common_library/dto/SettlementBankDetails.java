package com.himanshu.razorpay.common_library.dto;

public record SettlementBankDetails(
        String accountNumber,
        String ifsc,
        String accountHolderName
) {
}
