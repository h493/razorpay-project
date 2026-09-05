package com.himanshu.razorpay.vault_service.controller;

import com.himanshu.razorpay.common_library.dto.PaymentProcessorResponse;
import com.himanshu.razorpay.common_library.dto.VaultChargeRequest;
import com.himanshu.razorpay.vault_service.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/vault")
public class InternalVaultController {

    private final VaultService vaultService;

    @PostMapping("/charge")
    public PaymentProcessorResponse charge(@RequestBody VaultChargeRequest request){
        return vaultService.charge(request.token() , request.paymentId(), request.amount(), request.methodDetails());
    }
}
