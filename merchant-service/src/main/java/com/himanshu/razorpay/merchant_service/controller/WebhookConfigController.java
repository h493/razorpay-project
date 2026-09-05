package com.himanshu.razorpay.merchant_service.controller;


import com.himanshu.razorpay.common_library.context.MerchantContext;
import com.himanshu.razorpay.merchant_service.dto.request.UpdateWebhookConfigRequest;
import com.himanshu.razorpay.merchant_service.dto.response.WebhookConfigResponse;
import com.himanshu.razorpay.merchant_service.service.WebhookConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants/webhooks")
@RequiredArgsConstructor
public class WebhookConfigController {

    private final WebhookConfigService webhookConfigService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<WebhookConfigResponse> create(@Valid @RequestBody UpdateWebhookConfigRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(webhookConfigService.create(merchantContext.getMerchantId(), request));
    }

    @GetMapping
    public ResponseEntity<List<WebhookConfigResponse>> list() {
        return ResponseEntity.ok(webhookConfigService.list(merchantContext.getMerchantId()));
    }

    @GetMapping("/{configId}")
    public ResponseEntity<WebhookConfigResponse> getById(@PathVariable UUID configId) {
        return ResponseEntity.ok(webhookConfigService.getById(merchantContext.getMerchantId(), configId));
    }

    @PutMapping("/{configId}")
    public ResponseEntity<WebhookConfigResponse> update(@PathVariable UUID configId,
                                                        @Valid @RequestBody UpdateWebhookConfigRequest request) {
        return ResponseEntity.ok(webhookConfigService.update(merchantContext.getMerchantId(), configId, request));
    }

    @DeleteMapping("/{configId}")
    public ResponseEntity<Void> delete(@PathVariable UUID configId) {
        webhookConfigService.delete(merchantContext.getMerchantId(), configId);
        return ResponseEntity.noContent().build();
    }
}
