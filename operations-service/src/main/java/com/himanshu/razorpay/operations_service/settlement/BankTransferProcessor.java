package com.himanshu.razorpay.operations_service.settlement;

import com.himanshu.razorpay.common_library.entity.Money;
import com.himanshu.razorpay.operations_service.settlement.dto.BankTransferResult;

import java.util.UUID;

public interface BankTransferProcessor {

    BankTransferResult initiate(UUID settlementId, UUID merchantId, Money amount,
                                String bankAccount, String ifsc);
}
