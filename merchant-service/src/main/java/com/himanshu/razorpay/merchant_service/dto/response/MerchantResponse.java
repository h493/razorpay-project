package com.himanshu.razorpay.merchant_service.dto.response;


import com.himanshu.razorpay.common_library.enums.BusinessType;
import com.himanshu.razorpay.common_library.enums.MerchantStatus;

import java.util.UUID;

public record MerchantResponse(
        UUID id,
        String name,
        String email,
        String businessName,
        BusinessType businessType,
        MerchantStatus merchantStatus
) {
}
