package com.himanshu.razorpay.merchant_service.dto.request;

import com.himanshu.razorpay.common_library.enums.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MerchantSignupRequest (
        @NotNull(message = "Name should be provided")
        @Size(max = 50, message = "Name should not be more than 50 character long")
        String name,

        @Email
        @NotNull(message = "Email is required")
        String email,

        @NotNull(message = "Password is required")
        @Size(min = 8, message = "Password should be at least 8 character long")
        String password,

        @Size(max = 50, message = "Business name should not be more than 50 character long")
        String businessName,

        BusinessType businessType
){
}
