package com.himanshu.razorpay.vault_service.dto.response;


import com.himanshu.razorpay.common_library.enums.CardBrand;

public record TokenizeResponse(
        String token,
        String lastFour,
        CardBrand brand,
        Integer expiryMonth,
        Integer expiryYear
) {
}
