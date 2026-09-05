package com.himanshu.razorpay.merchant_service.service.impl;


import com.himanshu.razorpay.common_library.util.RandomizerUtil;
import com.himanshu.razorpay.merchant_service.dto.request.UpdateWebhookConfigRequest;
import com.himanshu.razorpay.merchant_service.dto.response.WebhookConfigResponse;
import com.himanshu.razorpay.merchant_service.entity.Merchant;
import com.himanshu.razorpay.merchant_service.entity.MerchantWebhookConfig;
import com.himanshu.razorpay.merchant_service.mapper.WebhookConfigMapper;
import com.himanshu.razorpay.merchant_service.repository.MerchantRepository;
import com.himanshu.razorpay.merchant_service.repository.WebhookConfigRepository;
import com.himanshu.razorpay.merchant_service.service.WebhookConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookConfigServiceImpl implements WebhookConfigService {

    private final MerchantRepository merchantRepository;
    private final WebhookConfigRepository webhookConfigRepository;
    private final BytesEncryptor bytesEncryptor;
    private final WebhookConfigMapper webhookConfigMapper;

    @Override
    public WebhookConfigResponse create(UUID merchantId, UpdateWebhookConfigRequest request) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new RuntimeException("Merchant not found with id: " + merchantId));

        String rawSecret = RandomizerUtil.randomBase64(32);
        byte[] rawSecretBytes = rawSecret.getBytes();
        String encryptedSecret = Base64.getEncoder()
                .encodeToString(bytesEncryptor.encrypt(rawSecretBytes));


        MerchantWebhookConfig config = MerchantWebhookConfig.builder()
                .merchant(merchant)
                .targetUrl(request.targetUrl())
                .enabled(true)
                .eventTypes(request.eventTypes())
                .webhookSecret(encryptedSecret)
                .build();

        config = webhookConfigRepository.save(config);

        return webhookConfigMapper.toResponse(config, rawSecret);
    }

    @Override
    public List<WebhookConfigResponse> list(UUID merchantId) {
        return webhookConfigRepository.findByMerchant_Id(merchantId).stream()
                .map(config -> webhookConfigMapper.toResponse(config, null))
                .toList();
    }

    @Override
    public WebhookConfigResponse getById(UUID merchantId, UUID configId) {
       MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);
       return webhookConfigMapper.toResponse(config, null);
    }

    @Override
    @Transactional
    public WebhookConfigResponse update(UUID merchantId, UUID configId, UpdateWebhookConfigRequest request) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);
        config.setTargetUrl(request.targetUrl());
        config.setEventTypes(request.eventTypes());
        log.info("Updating webhook config: {}", config.getId());
        return webhookConfigMapper.toResponse(config, null);
    }

    @Override
    @Transactional
    public void delete(UUID merchantId, UUID configId) {
        MerchantWebhookConfig config = requireOwnedConfig(merchantId, configId);
        webhookConfigRepository.delete(config);
        log.info("Deleted webhook config: {}", config.getId());
    }

    private MerchantWebhookConfig requireOwnedConfig(UUID merchantId, UUID configId){
        return webhookConfigRepository.findByIdAndMerchant_Id(configId, merchantId)
                .orElseThrow(() -> new RuntimeException("Config not found or not owned by merchant: " + merchantId));
    }

}
