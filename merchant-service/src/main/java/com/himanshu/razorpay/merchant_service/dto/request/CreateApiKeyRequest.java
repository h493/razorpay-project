package com.himanshu.razorpay.merchant_service.dto.request;


import com.himanshu.razorpay.common_library.enums.Environment;

public record CreateApiKeyRequest(
        Environment environment
) {
}
