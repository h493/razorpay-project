package com.himanshu.razorpay.merchant_service.service;


import com.himanshu.razorpay.merchant_service.dto.request.CreateApiKeyRequest;
import com.himanshu.razorpay.merchant_service.dto.response.ApiKeyCreateResponse;
import com.himanshu.razorpay.merchant_service.dto.response.ApiKeyResponse;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
    ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest request);

    List<ApiKeyResponse> listByMerchant(UUID merchantId);

    void revoke(UUID merchantId, UUID keyId);

    ApiKeyCreateResponse rotate(UUID merchantId, UUID keyId);
}
