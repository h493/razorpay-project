package com.himanshu.razorpay.merchant_service.controller;

import com.himanshu.razorpay.common_library.cache.ApiKeyCacheEntry;
import com.himanshu.razorpay.merchant_service.entity.ApiKey;
import com.himanshu.razorpay.merchant_service.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/api-keys")
public class InternalApiKeyController {

    private final ApiKeyRepository apiKeyRepository;

    @GetMapping("/{keyId}")
    public ApiKeyCacheEntry findByKeyId(@PathVariable String keyId) {
        ApiKey apiKey =  apiKeyRepository.findByKeyId(keyId)
                .orElseThrow(() -> new RuntimeException("API Key not found for keyId: " + keyId));

        return new ApiKeyCacheEntry(apiKey.getKeyId(), apiKey.getKeySecretHash(),
                apiKey.getPreviousKeySecretHash(), apiKey.getGracePeriodExpiresAt(),
                apiKey.getMerchant().getId(), apiKey.getEnvironment(),
                apiKey.isEnabled());
    }
}
