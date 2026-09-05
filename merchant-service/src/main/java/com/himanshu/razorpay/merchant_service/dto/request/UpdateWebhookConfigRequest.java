package com.himanshu.razorpay.merchant_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateWebhookConfigRequest(

        @NotBlank(message = "Webhook URL cannot be blank")
        @Size(max = 500)
        @Pattern(regexp = "^(https?://).+", message = "Webhook URL must be a valid URL starting with http:// or https://")
        String targetUrl,

        //NULL/blank/all subscribes to every event type
        @Size(max = 1000)
        String eventTypes

) {
}
