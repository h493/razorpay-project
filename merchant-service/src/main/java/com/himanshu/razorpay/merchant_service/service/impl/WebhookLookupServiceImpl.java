package com.himanshu.razorpay.merchant_service.service.impl;


import com.himanshu.razorpay.common_library.dto.SettlementBankDetails;
import com.himanshu.razorpay.common_library.dto.WebhookTarget;
import com.himanshu.razorpay.common_library.enums.MerchantStatus;
import com.himanshu.razorpay.common_library.exception.ResourceNotFoundException;
import com.himanshu.razorpay.merchant_service.api.MerchantLookupService;
import com.himanshu.razorpay.merchant_service.entity.Merchant;
import com.himanshu.razorpay.merchant_service.repository.MerchantRepository;
import com.himanshu.razorpay.merchant_service.repository.WebhookConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookLookupServiceImpl implements MerchantLookupService {

    private final MerchantRepository merchantRepository;
    private final WebhookConfigRepository webhookConfigRepository;
    private final BytesEncryptor bytesEncryptor;

    @Override
    public List<WebhookTarget> getActiveConfigsForEvent(UUID merchantId, String eventType) {
        return webhookConfigRepository.findByMerchant_IdAndEnabledTrue(merchantId).stream()
                .filter(config -> config.isSubscribedTo(eventType))
                .map(config -> {
                    byte[] cipherBytes = Base64.getDecoder().decode(config.getWebhookSecret());
                    byte[] decryptedSecretBytes = bytesEncryptor.decrypt(cipherBytes);
                    return new WebhookTarget(config.getId(), config.getTargetUrl(), new String(decryptedSecretBytes, StandardCharsets.UTF_8));
                })
                .toList();

    }

    @Override
    public List<UUID> listActiveMerchantIds() {
        return merchantRepository.findByStatus(MerchantStatus.ACTIVE)
                .stream()
                .map(Merchant::getId)
                .toList();

    }

    @Override
    public SettlementBankDetails getSettlementBankDetails(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant", merchantId));

        return new SettlementBankDetails(
                merchant.getSettlementBankAccount(),
                merchant.getSettlementBankIfsc(),
                merchant.getSettlementBankHolderName()
        );
    }
}
