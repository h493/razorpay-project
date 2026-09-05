package com.himanshu.razorpay.merchant_service.dto.response;

import java.util.UUID;

public record ApiKeyCreateResponse(
        UUID id,
        String keyId,
        String keySecret,
        String environment
) {
}
