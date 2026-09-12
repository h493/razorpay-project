package com.himanshu.razorpay.api_gateway_service.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthHandler {

    private final JwtVerifier jwtVerifier;

    public Map<String, String> authenticate(String token) {
        Claims claims;
        try {
            claims = jwtVerifier.verifyAccessToken(token);
        } catch (Exception e) {
            throw new GatewayAuthenticationException("Invalid token");
        }
        return Map.of(
                "X-Merchant-Id", jwtVerifier.extractMerchantId(claims),
                "X-User-Role", jwtVerifier.extractRole(claims)
        );
    }
}
